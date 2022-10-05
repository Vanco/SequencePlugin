package org.intellij.sequencer.ext.kotlin;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.Stack;
import org.intellij.sequencer.config.SequenceSettingsState;
import org.intellij.sequencer.diagram.Info;
import org.intellij.sequencer.openapi.*;
import org.intellij.sequencer.openapi.model.CallStack;
import org.intellij.sequencer.openapi.model.ClassDescription;
import org.intellij.sequencer.openapi.model.LambdaExprDescription;
import org.intellij.sequencer.openapi.model.MethodDescription;
import org.intellij.sequencer.util.MyPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.CallableDescriptor;
import org.jetbrains.kotlin.idea.caches.resolve.ResolutionUtils;
import org.jetbrains.kotlin.idea.codeInsight.DescriptorToSourceUtilsIde;
import org.jetbrains.kotlin.psi.*;
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall;
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class KtSequenceGenerator extends KtTreeVisitorVoid implements IGenerator {
    private static final Logger LOGGER = Logger.getInstance(KtSequenceGenerator.class);
    private final Stack<KtCallExpression> exprStack = new Stack<>();
    private final Stack<CallStack> callStack = new Stack<>();
    private final Stack<Integer> offsetStack = new Stack<>();

    private CallStack topStack;
    private CallStack currentStack;
    private final SequenceParams params;

    private final boolean SHOW_LAMBDA_CALL;


    public KtSequenceGenerator(SequenceParams params) {
        this(params, 0);
    }

    public KtSequenceGenerator(SequenceParams params, int offset) {
        this.params = params;
        if (offset > 0) offsetStack.push(offset);
        SHOW_LAMBDA_CALL = SequenceSettingsState.getInstance().SHOW_LAMBDA_CALL;
    }

    @Override
    public CallStack generate(PsiElement psiElement, CallStack parent) {
        if (parent != null) {
            topStack = parent;
            currentStack = topStack;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[generate]" + psiElement.getText());
        }
        if (psiElement instanceof KtFunction) {
            generate((KtFunction) psiElement);
        } else if (psiElement instanceof PsiMethod) {
            return generate((PsiMethod) psiElement);
        } else if (psiElement instanceof KtClass) {
            final int naviOffset = offsetStack.isEmpty() ? MyPsiUtil.findNaviOffset(psiElement) : offsetStack.pop();
            generateClass((KtClass) psiElement, naviOffset);
        } else if (psiElement instanceof KtObjectDeclaration) {
            return generateObject((KtObjectDeclaration) psiElement);
        } else if (psiElement instanceof PsiClass) {
            final int naviOffset = offsetStack.isEmpty() ? MyPsiUtil.findNaviOffset(psiElement) : offsetStack.pop();
            generateClass((PsiClass) psiElement, naviOffset);
        } else if (psiElement instanceof KtLambdaExpression) {
            generateLambda((KtLambdaExpression) psiElement);
        } else {
            psiElement.accept(this);
            LOGGER.warn("unsupported " + psiElement.getText());
        }

        return topStack;
    }

    private void generateLambda(KtLambdaExpression lambdaExpression) {
        MethodDescription method = createMethod(lambdaExpression);
        makeMethodCallExceptCurrentStackIsRecursive(method);
        super.visitLambdaExpression(lambdaExpression);
    }


    private CallStack generateObject(KtObjectDeclaration psiElement) {
        final Collection<KtNamedFunction> children = PsiTreeUtil.findChildrenOfAnyType(psiElement, KtNamedFunction.class);
        for (KtNamedFunction function : children) {
            generate(function);
            break; //fixme: What should do with others
        }
        return topStack;
    }

    private void generateClass(KtClass psiElement, int textOffset) {
        final MethodDescription method = createMethod(psiElement, textOffset);
        makeMethodCallExceptCurrentStackIsRecursive(method);
    }


    private void generateClass(PsiClass psiElement, int textOffset) {
        final MethodDescription method = createMethod(psiElement, textOffset);
        makeMethodCallExceptCurrentStackIsRecursive(method);
    }

    private CallStack generate(KtFunction ktFunction) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[generate KtFunction]" + ktFunction.getName());
        }

        ktFunction.accept(this);
        return topStack;
    }

    private CallStack generate(PsiMethod psiMethod) {
        final IGenerator sequenceGenerator =
                offsetStack.isEmpty()
                        ? GeneratorFactory.createGenerator(psiMethod.getLanguage(), params)
                        : GeneratorFactory.createGenerator(psiMethod.getLanguage(), params, offsetStack.pop());
        CallStack javaCall = sequenceGenerator.generate(psiMethod, currentStack);
        LOGGER.debug("[JAVACall]:" + javaCall.toString());
        if (topStack == null) {
            topStack = javaCall;
            currentStack = topStack;
        }
        return topStack;
    }

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[visitNamedFunction]" + function.getName());
        }
        final int naviOffset = offsetStack.isEmpty() ? function.getTextOffset() : offsetStack.pop();
        MethodDescription method = createMethod(function, naviOffset);
        if (makeMethodCallExceptCurrentStackIsRecursive(method)) return;
        super.visitNamedFunction(function);
    }

    @Override
    public void visitConstructorDelegationCall(@NotNull KtConstructorDelegationCall call) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[visitConstructorDelegationCall]" + call.getText());
        }
        super.visitConstructorDelegationCall(call);
        resolveAndCall(call);
    }

    @Override
    public void visitConstructorCalleeExpression(@NotNull KtConstructorCalleeExpression constructorCalleeExpression) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[visitConstructorCalleeExpression]" + constructorCalleeExpression.getText());
        }
        super.visitConstructorCalleeExpression(constructorCalleeExpression);
    }

    @Override
    public void visitPrimaryConstructor(@NotNull KtPrimaryConstructor constructor) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[visitPrimaryConstructor]" + constructor.getText());
        }
        final int naviOffset = offsetStack.isEmpty() ? constructor.getTextOffset() : offsetStack.pop();
        MethodDescription method = createMethod(constructor, naviOffset);
        if (makeMethodCallExceptCurrentStackIsRecursive(method)) return;

        // Find KtClassInitializer and call it before constructor
        @NotNull Collection<KtClassInitializer> initializers = PsiTreeUtil.findChildrenOfType(((KtConstructor<?>) constructor).getContainingClassOrObject(), KtClassInitializer.class);
        for (KtClassInitializer initializer : initializers) {
            initializer.accept(this);
        }

        super.visitPrimaryConstructor(constructor);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[visitSecondaryConstructor]" + constructor.getText());
        }
        final int naviOffset = offsetStack.isEmpty() ? constructor.getTextOffset() : offsetStack.pop();
        MethodDescription method = createMethod(constructor, naviOffset);
        if (makeMethodCallExceptCurrentStackIsRecursive(method)) return;
        super.visitSecondaryConstructor(constructor);
    }

    @Override
    public void visitCallExpression(@NotNull KtCallExpression expression) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[visitCallExpression]" + expression.getText());
        }
        if (MyPsiUtil.isComplexCall(expression)) {
            exprStack.push(expression);
            callStack.push(currentStack);
            super.visitCallExpression(expression);
            if (!exprStack.isEmpty()) {
                CallStack old = currentStack;
                final KtCallExpression pop = exprStack.pop();
                currentStack = callStack.pop();
                resolveAndCall(pop);
                currentStack = old;
            }
        } else {
            super.visitCallExpression(expression);
            resolveAndCall(expression);
        }

    }

    @Override
    public void visitObjectLiteralExpression(@NotNull KtObjectLiteralExpression expression) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[visitObjectLiteralExpression]" + expression.getText());
        }
        GeneratorFactory
                .createGenerator(expression.getLanguage(), params)
                .generate(expression.getObjectDeclaration(), currentStack);
    }

    @Override
    public void visitLambdaExpression(@NotNull KtLambdaExpression lambdaExpression) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[visitLambdaExpression]" + lambdaExpression.getText());
        }
        if (SHOW_LAMBDA_CALL) {
            GeneratorFactory
                    .createGenerator(lambdaExpression.getLanguage(), params)
                    .generate(lambdaExpression, currentStack);
        } else {
            super.visitLambdaExpression(lambdaExpression);
        }
    }

    @Override
    public void visitArgument(@NotNull KtValueArgument argument) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[visitArgument]" + argument.getText());
        }
        super.visitArgument(argument);
    }

    private void resolveAndCall(@NotNull KtCallElement expression) {
        PsiElement psiElement = resolveFunction(expression);
        methodCall(psiElement, MyPsiUtil.findNaviOffset(expression));
    }


    @Nullable
    private PsiElement resolveFunction(@NotNull KtCallElement expression) {
        ResolvedCall<? extends CallableDescriptor> resolvedCall = ResolutionUtils.resolveToCall(expression, BodyResolveMode.PARTIAL);
        if (resolvedCall == null) return null;
        CallableDescriptor candidateDescriptor = resolvedCall.getCandidateDescriptor();
        return DescriptorToSourceUtilsIde.INSTANCE.getAnyDeclaration(expression.getProject(), candidateDescriptor);
    }

    private void methodCall(PsiElement psiElement, int offset) {
        if (psiElement == null) return;
        //fixme: should support kotlin filter
        if (!params.getMethodFilter().allow(psiElement)) return;

        if (currentStack != null && currentStack.level() < params.getMaxDepth()) {
            CallStack oldStack = currentStack;
            int level = currentStack.level();
            LOGGER.debug("--> depth = " + level + " method = " + psiElement.getText());
            offsetStack.push(offset);
            generate(psiElement, null); // here, No NEW Generator created, call with null
            LOGGER.debug("<-- depth = " + level + " method = " + psiElement.getText());
            currentStack = oldStack;
        } else {
            final MethodDescription method = createMethod(psiElement, offset);
            makeMethodCallExceptCurrentStackIsRecursive(method);
        }
    }

    private MethodDescription createMethod(KtLambdaExpression expression) {
        ParamPair paramPair = extractParameters(expression.getValueParameters());

        String returnType = "Unit";

        Class<? extends PsiElement> method = ElementTypeFinder.EP_NAME.forLanguage(expression.getLanguage()).findMethod();
        PsiElement psiElement = PsiTreeUtil.getParentOfType(expression, method, false);

        final int offset = expression.getTextOffset();
        MethodDescription enclosedMethod = createMethod(psiElement, offset);

        return new LambdaExprDescription(enclosedMethod, returnType, paramPair.argNames, paramPair.argTypes, offset);
    }

    private MethodDescription createMethod(PsiElement psiElement, int offset) {
        if (psiElement instanceof KtClass) return createMethod((KtClass) psiElement, offset);
        else if (psiElement instanceof KtNamedFunction) return createMethod((KtNamedFunction) psiElement, offset);
        else if (psiElement instanceof PsiClass) return createMethod((PsiClass) psiElement, offset);
        else if (psiElement instanceof KtFunctionLiteral) return createMethod((KtFunctionLiteral) psiElement, offset);
        else LOGGER.debug("Unsupported PsiElement: ", psiElement.toString(), psiElement.getText());
        // todo Something else
        return MethodDescription.DUMMY_METHOD;
    }

    private MethodDescription createMethod(KtFunctionLiteral function, int offset) {

        return MethodDescription.DUMMY_METHOD;
    }

    private MethodDescription createMethod(KtNamedFunction function, int offset) {
        ParamPair paramPair = extractParameters(function.getValueParameters());
        ClassDescription classDescription;
        final KtFile containingKtFile = function.getContainingKtFile();
        String filename = containingKtFile.getPackageFqName() + "." + containingKtFile.getName();
        filename = filename.replace(".kt", "_kt");
        if (function.isTopLevel()) {
            classDescription = ClassDescription.getFileNameAsClass(filename); //ClassDescription.TOP_LEVEL_FUN;
        } else if (function.isLocal()) {
            classDescription = ClassDescription.getFileNameAsClass(filename + "#" + Constants.ANONYMOUS_CLASS_NAME);
        } else {
            if (function.getFqName() != null) {
                String className = function.getFqName().parent().asString();
                classDescription = new ClassDescription(className, new ArrayList<>());
            } else {
                final KtObjectDeclaration ktObjectDeclaration = PsiTreeUtil.getParentOfType(function, KtObjectDeclaration.class);
                if (ktObjectDeclaration != null && !ktObjectDeclaration.getSuperTypeListEntries().isEmpty()) {
                    final KtSuperTypeListEntry entry = ktObjectDeclaration.getSuperTypeListEntries().get(0);
                    String typeName = entry.getTypeAsUserType().getReferencedName();
                    classDescription = ClassDescription.getFileNameAsClass(filename + "#" + typeName);
                } else {
                    classDescription = ClassDescription.getFileNameAsClass(filename + "#" + Constants.ANONYMOUS_CLASS_NAME);
                }
            }
        }
        List<String> attributes = createAttributes(function.getModifierList());
        String returnType = "Unit";
        if (function.hasDeclaredReturnType()) {
            returnType = getType(function.getTypeReference());
        }

        return MethodDescription.createMethodDescription(
                classDescription,
                attributes, function.getName(), returnType,
                paramPair.argNames, paramPair.argTypes,
                offset
        );
    }

    private MethodDescription createMethod(KtSecondaryConstructor constructor, int offset) {
        ParamPair paramPair = extractParameters(constructor.getValueParameters());
        ClassDescription classDescription = new ClassDescription(constructor.getContainingClassOrObject().getFqName().asString(), new ArrayList<>());

        List<String> attributes = createAttributes(constructor.getModifierList());
        String returnType = constructor.getName();

        return MethodDescription.createMethodDescription(
                classDescription,
                attributes, Constants.CONSTRUCTOR_METHOD_NAME, returnType,
                paramPair.argNames, paramPair.argTypes,
                offset
        );
    }

    private MethodDescription createMethod(KtPrimaryConstructor constructor, int offset) {
        ParamPair paramPair = extractParameters(constructor.getValueParameters());
        ClassDescription classDescription = new ClassDescription(constructor.getContainingClassOrObject().getFqName().asString(), new ArrayList<>());

        List<String> attributes = createAttributes(constructor.getModifierList());
        String returnType = constructor.getName();

        return MethodDescription.createMethodDescription(
                classDescription,
                attributes, Constants.CONSTRUCTOR_METHOD_NAME, returnType,
                paramPair.argNames, paramPair.argTypes,
                offset
        );
    }

    private MethodDescription createMethod(KtClass ktClass, int offset) {
        ClassDescription classDescription = new ClassDescription(ktClass.getFqName().asString(), new ArrayList<>());
        List<String> attributes = createAttributes(ktClass.getModifierList());
        String returnType = ktClass.getName();

        return MethodDescription.createMethodDescription(
                classDescription,
                attributes, Constants.CONSTRUCTOR_METHOD_NAME, returnType,
                new ArrayList<>(), new ArrayList<>(),
                offset
        );
    }

    private MethodDescription createMethod(PsiClass psiClass, int offset) {
        ClassDescription classDescription = new ClassDescription(psiClass.getQualifiedName(), new ArrayList<>());
        List<String> attributes = createAttributes(psiClass.getModifierList(), MyPsiUtil.isExternal(psiClass));
        String returnType = psiClass.getName();

        return MethodDescription.createMethodDescription(
                classDescription,
                attributes, Constants.CONSTRUCTOR_METHOD_NAME, returnType,
                new ArrayList<>(), new ArrayList<>(),
                offset
        );
    }

    private List<String> createAttributes(PsiModifierList psiModifierList, boolean external) {
        if (psiModifierList == null)
            return Collections.emptyList();

        List<String> attributes = new ArrayList<>();
        for (int i = 0; i < Info.RECOGNIZED_METHOD_ATTRIBUTES.length; i++) {
            String attribute = Info.RECOGNIZED_METHOD_ATTRIBUTES[i];
            if (psiModifierList.hasModifierProperty(attribute))
                attributes.add(attribute);
        }
        if (external)
            attributes.add(Info.EXTERNAL_ATTRIBUTE);
        if (MyPsiUtil.isInterface(psiModifierList))
            attributes.add(Info.INTERFACE_ATTRIBUTE);
        return attributes;
    }

    private List<String> createAttributes(KtModifierList modifierList) {

        return new ArrayList<>();
    }

    private ParamPair extractParameters(List<KtParameter> parameters) {
        List<String> argNames = new ArrayList<>();
        List<String> argTypes = new ArrayList<>();
        for (KtParameter parameter : parameters) {
            argNames.add(parameter.getName());
            argTypes.add(getType(parameter.getTypeReference()));
        }
        return new ParamPair(argNames, argTypes);
    }

    /**
     * Get Type reference string.
     *
     * @param typeReference KtTypeReference
     * @return String
     */
    private String getType(@Nullable KtTypeReference typeReference) {
        if (typeReference == null) {
            return "Unit";
        }

        KtTypeElement typeElement = typeReference.getTypeElement();

        if (typeElement instanceof KtNullableType) {
            typeElement = ((KtNullableType) typeElement).getInnerType();
        }

        if (typeElement instanceof KtUserType) {
            return ((KtUserType) typeElement).getReferencedName();
        }

        if (typeElement instanceof KtFunctionType) {
            return typeElement.getText().replaceAll("[\\(|\\)]", "_").replaceAll(" ", "");//.replaceAll("->", "→");
        }

        return "Unit";
    }

    private boolean makeMethodCallExceptCurrentStackIsRecursive(MethodDescription method) {
        if (method == null) return false;

        if (topStack == null) {
            topStack = new CallStack(method);
            currentStack = topStack;
        } else {
            if (params.isNotAllowRecursion() && currentStack.isRecursive(method))
                return true;
            currentStack = currentStack.methodCall(method);
        }
        return false;
    }

}

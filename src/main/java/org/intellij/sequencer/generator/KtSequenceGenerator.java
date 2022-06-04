package org.intellij.sequencer.generator;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.Stack;
import org.intellij.sequencer.Constants;
import org.intellij.sequencer.util.MyPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.CallableDescriptor;
import org.jetbrains.kotlin.idea.caches.resolve.ResolutionUtils;
import org.jetbrains.kotlin.psi.*;
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils;
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall;
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KtSequenceGenerator extends KtTreeVisitorVoid implements IGenerator {
    private static final Logger LOGGER = Logger.getInstance(KtSequenceGenerator.class.getName());
    private final Stack<KtCallExpression> exprStack = new Stack<>();
    private final Stack<CallStack> callStack = new Stack<>();
    private final Stack<Integer> offsetStack = new Stack<>();

    private CallStack topStack;
    private CallStack currentStack;
    private int depth;
    private final SequenceParams params;

    public KtSequenceGenerator(SequenceParams params) {
        this.params = params;
    }

    public KtSequenceGenerator(SequenceParams params, int offset, int depth) {
        this(params);
        offsetStack.push(offset);
        this.depth = depth;

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
            return generate((KtFunction) psiElement);
        } else if (psiElement instanceof PsiMethod) {
            return generate((PsiMethod) psiElement);
        } else if (psiElement instanceof KtClass) {
            final int naviOffset = offsetStack.isEmpty() ? MyPsiUtil.findNaviOffset(psiElement) : offsetStack.pop();
            generateClass((KtClass) psiElement, naviOffset);
        } else if (psiElement instanceof KtObjectDeclaration) {
            return generateObject((KtObjectDeclaration) psiElement);
        } else {
            psiElement.accept(this);
            LOGGER.warn("unsupported " + psiElement.getText());
        }

        return topStack;
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

    private CallStack generate(KtFunction ktFunction) {
        ktFunction.accept(this);
        return topStack;
    }

    private CallStack generate(PsiMethod psiMethod) {
        final SequenceGenerator sequenceGenerator =
                offsetStack.isEmpty() ? new SequenceGenerator(params) : new SequenceGenerator(params, offsetStack.pop(), depth);
        CallStack javaCall = sequenceGenerator.generate(psiMethod, currentStack);
        LOGGER.debug("[JAVACall]:" + javaCall.generateText());
        if (topStack == null) {
            topStack = javaCall;
            currentStack = topStack;
        }
        return topStack;
    }

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[visitNamedFunction]" + function.getText());
        }
        final int naviOffset = offsetStack.isEmpty() ? function.getTextOffset() : offsetStack.pop();
        MethodDescription method = createMethod(function, naviOffset);
        if (makeMethodCallExceptCurrentStackIsRecursive(method)) return;
        super.visitNamedFunction(function);
    }

    @Override
    public void visitPrimaryConstructor(@NotNull KtPrimaryConstructor constructor) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[visitPrimaryConstructor]" + constructor.getText());
        }
        MethodDescription method = createMethod(constructor);
        if (makeMethodCallExceptCurrentStackIsRecursive(method)) return;
        super.visitPrimaryConstructor(constructor);
    }

    @Override
    public void visitSecondaryConstructor(@NotNull KtSecondaryConstructor constructor) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[visitSecondaryConstructor]" + constructor.getText());
        }
        MethodDescription method = createMethod(constructor);
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
        CallStack objLiteralExpr = new KtSequenceGenerator(params).generate(expression.getObjectDeclaration(), currentStack);
        //currentStack = currentStack.methodCall(objLiteralExpr);
        //super.visitObjectLiteralExpression(expression);
    }

    private void resolveAndCall(@NotNull KtCallExpression expression) {
        PsiElement psiElement = resolveFunction(expression);
        methodCall(psiElement, MyPsiUtil.findNaviOffset(expression));
    }


    @Nullable
    private PsiElement resolveFunction(@NotNull KtExpression expression) {
        ResolvedCall<? extends CallableDescriptor> resolvedCall = ResolutionUtils.resolveToCall(expression, BodyResolveMode.PARTIAL);
        if (resolvedCall == null) return null;
        CallableDescriptor candidateDescriptor = resolvedCall.getCandidateDescriptor();
        return DescriptorToSourceUtils.descriptorToDeclaration(candidateDescriptor);
    }

    private void methodCall(PsiElement psiElement, int offset) {
        if (psiElement == null) return;
        //fixme: should support kotlin filter
        if (psiElement instanceof PsiMethod && !params.getMethodFilter().allow((PsiMethod) psiElement)) return;

        if (depth < params.getMaxDepth() - 1) {
            CallStack oldStack = currentStack;
            depth++;
            LOGGER.debug("+ depth = " + depth + " method = " + psiElement.getText());
            offsetStack.push(offset);
            generate(psiElement,null); // here, No NEW Generator created, call with null
            depth--;
            LOGGER.debug("- depth = " + depth + " method = " + psiElement.getText());
            currentStack = oldStack;
        } else {
            final MethodDescription method = createMethod(psiElement, offset);
            if (method != null) //fixme
                currentStack.methodCall(method);
        }
    }

    private MethodDescription createMethod(PsiElement psiElement, int offset) {
        if (psiElement instanceof KtClass) return createMethod((KtClass) psiElement, offset);
        else if (psiElement instanceof KtNamedFunction) return createMethod((KtNamedFunction) psiElement, offset);
        // todo Something else
        return null;
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

    private MethodDescription createMethod(KtSecondaryConstructor constructor) {
        ParamPair paramPair = extractParameters(constructor.getValueParameters());
        ClassDescription classDescription = new ClassDescription(constructor.getName(), new ArrayList<>());

        List<String> attributes = createAttributes(constructor.getModifierList());
        String returnType = constructor.getName();

        return MethodDescription.createMethodDescription(
                classDescription,
                attributes, Constants.CONSTRUCTOR_METHOD_NAME, returnType,
                paramPair.argNames, paramPair.argTypes,
                constructor.getTextOffset()
        );
    }

    private MethodDescription createMethod(KtPrimaryConstructor constructor) {
        ParamPair paramPair = extractParameters(constructor.getValueParameters());
        ClassDescription classDescription = new ClassDescription(constructor.getName(), new ArrayList<>());

        List<String> attributes = createAttributes(constructor.getModifierList());
        String returnType = constructor.getName();

        return MethodDescription.createMethodDescription(
                classDescription,
                attributes, Constants.CONSTRUCTOR_METHOD_NAME, returnType,
                paramPair.argNames, paramPair.argTypes,
                constructor.getTextOffset()
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

    private static class ParamPair {
        final List<String> argNames;
        final List<String> argTypes;

        public ParamPair(List<String> argNames, List<String> argTypes) {
            this.argNames = argNames;
            this.argTypes = argTypes;
        }
    }
}

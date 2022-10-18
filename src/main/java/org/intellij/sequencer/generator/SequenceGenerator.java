package org.intellij.sequencer.generator;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.util.containers.Stack;
import org.intellij.sequencer.config.SequenceSettingsState;
import org.intellij.sequencer.diagram.Info;
import org.intellij.sequencer.generator.filters.ImplementClassFilter;
import org.intellij.sequencer.openapi.GeneratorFactory;
import org.intellij.sequencer.openapi.IGenerator;
import org.intellij.sequencer.openapi.SequenceParams;
import org.intellij.sequencer.openapi.model.CallStack;
import org.intellij.sequencer.openapi.model.ClassDescription;
import org.intellij.sequencer.openapi.model.LambdaExprDescription;
import org.intellij.sequencer.openapi.model.MethodDescription;
import org.intellij.sequencer.util.MyPsiUtil;
import org.jetbrains.kotlin.idea.KotlinLanguage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SequenceGenerator extends JavaRecursiveElementVisitor implements IGenerator {
    private static final Logger LOGGER = Logger.getInstance(SequenceGenerator.class);
    private final Stack<Integer> offsetStack = new Stack<>();

    private final ArrayList<String> imfCache = new ArrayList<>();
    private CallStack topStack;
    private CallStack currentStack;
    private final SequenceParams params;

    private final boolean SHOW_LAMBDA_CALL;

    public SequenceGenerator(SequenceParams params) {
        this.params = params;
        SHOW_LAMBDA_CALL = SequenceSettingsState.getInstance().SHOW_LAMBDA_CALL;
    }

    public SequenceGenerator(SequenceParams params, int offset) {
        this(params);
        offsetStack.push(offset);
    }

    @Override
    public CallStack generate(PsiElement psiElement, CallStack parent) {
        if (parent != null) {
            topStack = parent;
            currentStack = topStack;
        }

        if (psiElement instanceof PsiMethod)
            return generate((PsiMethod) psiElement);
        else if (psiElement instanceof PsiLambdaExpression) {
            return generate((PsiLambdaExpression) psiElement);
        } else {
            LOGGER.warn("unsupported " + psiElement.getText());
        }

        return topStack;
    }

    /**
     * Generate lambda expression separately.
     *
     * @param expression lambda expression
     * @return CallStack
     */
    private CallStack generate(PsiLambdaExpression expression) {
        MethodDescription method = createMethod(expression);
        makeMethodCallExceptCurrentStackIsRecursive(method);
        super.visitLambdaExpression(expression);
        return topStack;
    }

    private CallStack generate(PsiMethod psiMethod) {
        if (psiMethod.getLanguage().equals(JavaLanguage.INSTANCE)) {
            return generateJava(psiMethod);
        } else if (psiMethod.getLanguage().equals(KotlinLanguage.INSTANCE)) {
            return generateKotlin(psiMethod);
        } else {
            return topStack;
        }
    }

    /**
     * Generate kotlin call in java code. Invoke `KtSequenceGenerator` to generate subtree of `CallStack`.
     *
     * @param psiMethod kotlin fun
     * @return CallStack
     */
    private CallStack generateKotlin(PsiMethod psiMethod) {

        final IGenerator ktSequenceGenerator =
                offsetStack.isEmpty()
                        ? GeneratorFactory.createGenerator(psiMethod.getLanguage(), params)
                        : GeneratorFactory.createGenerator(psiMethod.getLanguage(), params, offsetStack.pop());
        CallStack kotlinCall = ktSequenceGenerator.generate(psiMethod.getNavigationElement(), currentStack);
        if (topStack == null) {
            topStack = kotlinCall;
            currentStack = topStack;
        }
        return topStack;
    }

    /**
     * Generate Java method
     * @param psiMethod Java method
     * @return CallStack
     */
    private CallStack generateJava(PsiMethod psiMethod) {
        PsiClass containingClass = psiMethod.getContainingClass();
        if (containingClass == null) {
            containingClass = (PsiClass) psiMethod.getParent().getContext();
        }

        if (containingClass == null) {
            return topStack;
        }

        // follow implementation
        if (MyPsiUtil.isAbstract(containingClass)) {
            psiMethod.accept(this);
            PsiElement[] psiElements = DefinitionsScopedSearch.search(psiMethod).toArray(PsiElement.EMPTY_ARRAY);
            if (psiElements.length == 1) {
                methodAccept(psiElements[0]);
            } else {
                for (PsiElement psiElement : psiElements) {
                    if (psiElement instanceof PsiMethod) {
                        if (alreadyInStack((PsiMethod) psiElement)) continue;

                        if (/*!params.isSmartInterface() && */params.getImplementationWhiteList().allow(psiElement))
                            methodAccept(psiElement);
                    }
                }
            }
        } else {
            // resolve variable initializer
            if (/*params.isSmartInterface() && */!MyPsiUtil.isExternal(containingClass) && !imfCache.contains(containingClass.getQualifiedName())) {
                containingClass.accept(new ImplementationFinder());
                imfCache.add(containingClass.getQualifiedName());
            }

            psiMethod.accept(this);
        }
        return topStack;
    }

    private boolean alreadyInStack(PsiMethod psiMethod) {
        // Don't check external method, because the getTextOffset() will cause Java decompiler, it will wast of time.
        if (psiMethod.getContainingClass() == null || MyPsiUtil.isExternal(psiMethod.getContainingClass())) return true;
        final int offset = psiMethod.getTextOffset();
        MethodDescription method = createMethod(psiMethod, offset);
        return currentStack.isRecursive(method);
    }

    private void methodAccept(PsiElement psiElement) {
        if (psiElement instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) psiElement;
            if (params.getMethodFilter().allow(method)) {
                PsiClass containingClass = (method).getContainingClass();
                if (/*params.isSmartInterface() && */containingClass != null && !MyPsiUtil.isExternal(containingClass) && !imfCache.contains(containingClass.getQualifiedName())) {
                    containingClass.accept(new ImplementationFinder());
                    imfCache.add(containingClass.getQualifiedName());
                }
                method.accept(this);
            }
        }
    }

    public void visitMethod(PsiMethod psiMethod) {
        int offset = offsetStack.isEmpty() ? psiMethod.getTextOffset() : offsetStack.pop();
        MethodDescription method = createMethod(psiMethod, offset);
        if (makeMethodCallExceptCurrentStackIsRecursive(method)) return;
        super.visitMethod(psiMethod);
    }

    @Override
    public void visitCallExpression(PsiCallExpression callExpression) {
        super.visitCallExpression(callExpression);
        PsiMethod psiMethod = callExpression.resolveMethod();
        findAbstractImplFilter(callExpression, psiMethod);
        methodCall(psiMethod, MyPsiUtil.findNaviOffset(callExpression));
    }

    @Override
    public void visitMethodReferenceExpression(PsiMethodReferenceExpression expression) {
        final PsiElement resolve = expression.resolve();
        if (resolve instanceof PsiMethod) {
            final PsiMethod psiMethod = (PsiMethod) resolve;
            final int offset = expression.getTextOffset();
            methodCall(psiMethod, offset);
        }
        super.visitMethodReferenceExpression(expression);
    }

    /**
     * If the psiMethod's containing class is Interface or abstract, then try to find its implement class.
     *
     * @param callExpression expression
     * @param psiMethod      method
     */
    private void findAbstractImplFilter(PsiCallExpression callExpression, PsiMethod psiMethod) {
        try {
            PsiClass containingClass = psiMethod.getContainingClass();
            if (MyPsiUtil.isAbstract(containingClass)) {
                String type = containingClass.getQualifiedName();
                if (type == null) return;

                PsiMethodCallExpression psiMethodCallExpression = (PsiMethodCallExpression) callExpression;
                PsiExpression qualifierExpression = psiMethodCallExpression.getMethodExpression().getQualifierExpression();

                if (qualifierExpression == null) return;

                PsiType psiType = qualifierExpression.getType();

                if (psiType == null) return;

                String impl = psiType.getCanonicalText();

                if (!impl.equals(type))
                    params.getImplementationWhiteList().putIfAbsent(type, new ImplementClassFilter(impl));
            }
        } catch (Exception e) {
            //ignore
        }
    }

    private void methodCall(PsiMethod psiMethod, int offset) {
        if (psiMethod == null) return;
        if (!params.getMethodFilter().allow(psiMethod)) return;

        if (currentStack.level() < params.getMaxDepth()) {
            CallStack oldStack = currentStack;
            LOGGER.debug("+ depth = " + currentStack.level() + " method = " + psiMethod.getName());
            offsetStack.push(offset);
            generate(psiMethod);
            LOGGER.debug("- depth = " + currentStack.level() + " method = " + psiMethod.getName());
            currentStack = oldStack;
        } else
            currentStack.methodCall(createMethod(psiMethod, offset));
    }

    private MethodDescription createMethod(PsiMethod psiMethod, int offset) {

        ParamPair paramPair = extractParameters(psiMethod.getParameterList());

        PsiClass containingClass = psiMethod.getContainingClass();
        if (containingClass == null) {
            containingClass = (PsiClass) psiMethod.getParent().getContext();
        }

        Objects.requireNonNull(containingClass);

        List<String> attributes = createAttributes(psiMethod.getModifierList(), MyPsiUtil.isExternal(containingClass));
        if (psiMethod.isConstructor())
            return MethodDescription.createConstructorDescription(
                    createClassDescription(containingClass),
                    attributes, paramPair.argNames, paramPair.argTypes, offset);

        PsiType returnType = psiMethod.getReturnType();
        Objects.requireNonNull(returnType);

        return MethodDescription.createMethodDescription(
                createClassDescription(containingClass),
                attributes, psiMethod.getName(), returnType.getCanonicalText(),
                paramPair.argNames, paramPair.argTypes, offset);
    }

    private ParamPair extractParameters(PsiParameterList parameterList) {
        PsiParameter[] parameters = parameterList.getParameters();
        List<String> argNames = new ArrayList<>();
        List<String> argTypes = new ArrayList<>();
        for (PsiParameter parameter : parameters) {
            argNames.add(parameter.getName());
            PsiType psiType = parameter.getType();
            argTypes.add(psiType.getCanonicalText());
        }
        return new ParamPair(argNames, argTypes);
    }

    private ClassDescription createClassDescription(PsiClass psiClass) {
        return new ClassDescription(psiClass.getQualifiedName(),
                createAttributes(psiClass.getModifierList(), MyPsiUtil.isExternal(psiClass)));
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

    @Override
    public void visitLocalVariable(PsiLocalVariable variable) {
        PsiJavaCodeReferenceElement referenceElement = variable.getTypeElement().getInnermostComponentReferenceElement();

        variableImplementationFinder(referenceElement, variable.getType(), variable.getInitializer());

        super.visitLocalVariable(variable);
    }

    /**
     * Find interface's implementation of which user had used in assignment. e.g.
     * <pre>
     * public interface Fruit {
     *     int eat();
     * }
     *
     * public class Apple implements Fruit {
     *     @Override
     *     public int eat() {
     *         return 5;
     *     }
     * }
     *
     * public class Banana implements Fruit {
     *      @Override
     *      public int eat() {
     *          return 1;
     *      }
     * }
     * </pre>
     *
     * When user use Apple assignment, the <code>Apple</code> implement should be preferred.
     * <pre>
     *     Fruit fruit = new Apple()
     * </pre>
     *
     * @param referenceElement
     * @param psiType
     * @param initializer
     */
    private void variableImplementationFinder(PsiJavaCodeReferenceElement referenceElement, PsiType psiType, PsiExpression initializer) {
        if (referenceElement != null) {
            PsiClass psiClass = (PsiClass) referenceElement.resolve();

            if (MyPsiUtil.isAbstract(psiClass)) {
                String face = psiType.getCanonicalText();
                if (initializer instanceof PsiNewExpression) {
                    PsiType initializerType = initializer.getType();
                    if (initializerType != null) {
                        ArrayList<String> list = new ArrayList<>();
                        String impl = initializerType.getCanonicalText();
                        // initializer type is not same as variable type
                        if (!face.equals(impl)) {
                            list.add(impl);
                        }

                        PsiType[] superTypes = initializerType.getSuperTypes();
                        for (PsiType superType : superTypes) {
                            String superImpl = superType.getCanonicalText();
                            if (!face.equals(superImpl)) {
                                list.add(superImpl);
                            }
                        }

                        if (!list.isEmpty()) {
                            params.getImplementationWhiteList().putIfAbsent(face, new ImplementClassFilter(list.toArray(new String[0])));
                        }
                    }

                }
            }
        }
    }

    @Override
    public void visitAssignmentExpression(PsiAssignmentExpression expression) {
        findImplementationInAssignmentExpression(expression);
        super.visitAssignmentExpression(expression);
    }

    private void findImplementationInAssignmentExpression(PsiAssignmentExpression expression) {
        PsiExpression re = expression.getRExpression();
        if (re instanceof PsiNewExpression) {
            PsiType type = expression.getType();
            if (type == null) return;

            String face = type.getCanonicalText();
            ArrayList<String> list = new ArrayList<>();

            PsiType psiType = re.getType();
            if (psiType == null) return;

            String impl = psiType.getCanonicalText();

            if (!face.equals(impl)) {
                list.add(impl);
            }

            PsiType[] superTypes = psiType.getSuperTypes();
            for (PsiType superType : superTypes) {
                String superImpl = superType.getCanonicalText();
                if (!face.equals(superImpl)) {
                    list.add(superImpl);
                }
            }

            if (!list.isEmpty()) {
                params.getImplementationWhiteList().putIfAbsent(face, new ImplementClassFilter(list.toArray(new String[0])));
            }
        }
    }

    @Override
    public void visitLambdaExpression(PsiLambdaExpression expression) {
        if (SHOW_LAMBDA_CALL) {
            GeneratorFactory
                    .createGenerator(expression.getLanguage(), params)
                    .generate(expression, currentStack);
        } else {
            super.visitLambdaExpression(expression);
        }
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

    private MethodDescription createMethod(PsiLambdaExpression expression) {

        ParamPair paramPair = extractParameters(expression.getParameterList());

        String returnType;
        PsiType functionalInterfaceType = expression.getFunctionalInterfaceType();
        if (functionalInterfaceType == null) {
            returnType = null;
        } else {
            returnType = functionalInterfaceType.getCanonicalText();
        }

        PsiMethod psiMethod = MyPsiUtil.findEnclosedPsiMethod(expression);
        final int offset = expression.getTextOffset();
        MethodDescription enclosedMethod = createMethod(psiMethod, offset);

        return new LambdaExprDescription(enclosedMethod, returnType, paramPair.argNames, paramPair.argTypes, offset);
    }

    @Override
    public void visitInstanceOfExpression(PsiInstanceOfExpression expression) {
        super.visitInstanceOfExpression(expression);
    }

    private class ImplementationFinder extends JavaElementVisitor {

        @Override
        public void visitClass(PsiClass aClass) {
            for (PsiClass psiClass : aClass.getSupers()) {
                if (!MyPsiUtil.isExternal(psiClass))
                    psiClass.accept(this);
            }

            if (!MyPsiUtil.isAbstract(aClass) && !MyPsiUtil.isExternal(aClass)) {
                super.visitClass(aClass);
            }
        }

        @Override
        public void visitField(PsiField field) {
            PsiTypeElement typeElement = field.getTypeElement();
            if (typeElement != null) {
                PsiJavaCodeReferenceElement referenceElement = typeElement.getInnermostComponentReferenceElement();
                variableImplementationFinder(referenceElement, field.getType(), field.getInitializer());
            }

            super.visitField(field);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            // only constructor
//            PsiClass containingClass = method.getContainingClass();
//            if (containingClass != null && method.getName().equals(containingClass.getName())) {
//                super.visitMethod(method);
//            }
            if (method.isConstructor()) {
                super.visitMethod(method);
            }
        }

        @Override
        public void visitAssignmentExpression(PsiAssignmentExpression expression) {
            findImplementationInAssignmentExpression(expression);
            super.visitAssignmentExpression(expression);
        }

        public void visitElement(PsiElement psiElement) {
            psiElement.acceptChildren(this);
        }

    }
}

package org.intellij.sequencer.generator;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.util.containers.Stack;
import org.intellij.sequencer.diagram.Info;
import org.intellij.sequencer.generator.filters.ImplementClassFilter;
import org.intellij.sequencer.util.MyPsiUtil;
import org.jetbrains.kotlin.idea.KotlinLanguage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SequenceGenerator extends JavaRecursiveElementVisitor implements IGenerator {
    private final Stack<Integer> offsetStack = new Stack<>();

    private static final Logger LOGGER = Logger.getInstance(SequenceGenerator.class.getName());

    private final ImplementationFinder implementationFinder = new ImplementationFinder();
    private CallStack topStack;
    private CallStack currentStack;
    private int depth;
    private final SequenceParams params;

    public SequenceGenerator(SequenceParams params) {
        this.params = params;
    }

    public SequenceGenerator(SequenceParams params, int offset, int depth) {
        this(params);
        offsetStack.push(offset);
        this.depth = depth;
    }

    @Override
    public CallStack generate(PsiElement psiElement) {
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
    public CallStack generate(PsiLambdaExpression expression) {
        MethodDescription method = createMethod(expression);
        makeMethodCallExceptCurrentStackIsRecursive(method);
        super.visitLambdaExpression(expression);
        return topStack;
    }

    public CallStack generate(PsiMethod psiMethod) {
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

        final KtSequenceGenerator ktSequenceGenerator =
                offsetStack.isEmpty() ? new KtSequenceGenerator(params) : new KtSequenceGenerator(params, offsetStack.pop(), depth);
        CallStack kotlinCall = ktSequenceGenerator.generate(psiMethod.getNavigationElement());
        if (topStack == null) {
            topStack = kotlinCall;
            currentStack = topStack;
        } else {
            currentStack.merge(kotlinCall);
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

                        if (/*!params.isSmartInterface() && */params.getInterfaceImplFilter().allow((PsiMethod) psiElement))
                            methodAccept(psiElement);
                    }
                }
            }
        } else {
            // resolve variable initializer
            if (params.isSmartInterface() && !MyPsiUtil.isExternal(containingClass)) {
                containingClass.accept(implementationFinder);
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
                if (params.isSmartInterface() && containingClass != null && !MyPsiUtil.isExternal(containingClass))
                    containingClass.accept(implementationFinder);
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

                if (!impl.startsWith(type))
                    params.getInterfaceImplFilter().put(type, new ImplementClassFilter(impl));
            }
        } catch (Exception e) {
            //ignore
        }
    }

    private void methodCall(PsiMethod psiMethod, int offset) {
        if (psiMethod == null) return;
        if (!params.getMethodFilter().allow(psiMethod)) return;

        if (depth < params.getMaxDepth() - 1) {
            CallStack oldStack = currentStack;
            depth++;
            LOGGER.debug("+ depth = " + depth + " method = " + psiMethod.getName());
            offsetStack.push(offset);
            generate(psiMethod);
            depth--;
            LOGGER.debug("- depth = " + depth + " method = " + psiMethod.getName());
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

    private void variableImplementationFinder(PsiJavaCodeReferenceElement referenceElement, PsiType psiType, PsiExpression initializer) {
        if (referenceElement != null) {
            PsiClass psiClass = (PsiClass) referenceElement.resolve();

            if (MyPsiUtil.isAbstract(psiClass)) {
                String type = psiType.getCanonicalText();
                if (initializer instanceof PsiNewExpression) {
                    PsiType initializerType = initializer.getType();
                    if (initializerType != null) {
                        String impl = initializerType.getCanonicalText();
                        if (!type.equals(impl)) {
                            params.getInterfaceImplFilter().put(type, new ImplementClassFilter(impl));
                        }
                    }

                }
            }
        }
    }

    @Override
    public void visitAssignmentExpression(PsiAssignmentExpression expression) {
        PsiExpression re = expression.getRExpression();
        if (params.isSmartInterface() && re instanceof PsiNewExpression) {
            String face = Objects.requireNonNull(expression.getType()).getCanonicalText();
            PsiType psiType = Objects.requireNonNull(expression.getRExpression()).getType();
            String impl = Objects.requireNonNull(psiType).getCanonicalText();

            params.getInterfaceImplFilter().put(face, new ImplementClassFilter(impl));

        }
        super.visitAssignmentExpression(expression);
    }

    @Override
    public void visitLambdaExpression(PsiLambdaExpression expression) {
        CallStack lambdaExpr = new SequenceGenerator(params).generate(expression);
        currentStack = currentStack.merge(lambdaExpr);
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
            PsiClass containingClass = method.getContainingClass();
            if (containingClass != null && method.getName().equals(containingClass.getName())) {
                super.visitMethod(method);
            }
        }

        @Override
        public void visitAssignmentExpression(PsiAssignmentExpression expression) {
            PsiExpression re = expression.getRExpression();
            if (re instanceof PsiNewExpression) {
                String face = Objects.requireNonNull(expression.getType()).getCanonicalText();
                String impl = Objects.requireNonNull(expression.getRExpression().getType()).getCanonicalText();

                params.getInterfaceImplFilter().put(face, new ImplementClassFilter(impl));

            }
            super.visitAssignmentExpression(expression);
        }

        public void visitElement(PsiElement psiElement) {
            psiElement.acceptChildren(this);
        }

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

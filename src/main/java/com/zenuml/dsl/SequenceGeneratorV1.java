package com.zenuml.dsl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.util.containers.Stack;
import org.intellij.sequencer.diagram.Info;
import org.intellij.sequencer.generator.SequenceParams;
import org.intellij.sequencer.generator.filters.ImplementClassFilter;
import org.intellij.sequencer.util.PsiUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SequenceGeneratorV1 extends JavaElementVisitor {
    private final Stack<PsiCallExpression> _exprStack = new Stack<PsiCallExpression>();
    private final Stack<CallStack> _callStack = new Stack<>();
    private static final Logger LOGGER = Logger.getInstance(SequenceGeneratorV1.class.getName());

    private final ImplementationFinder implementationFinder = new ImplementationFinder();
    private CallStack topStack;
    private CallStack currentStack;
    private int depth;
    private SequenceParams params;
    private SequenceDiagram sequenceDiagram;

    public SequenceGeneratorV1(SequenceParams params) {
        this.params = params;
        this.sequenceDiagram = new SequenceDiagram();
    }

    public void generate(PsiMethod psiMethod) {
        PsiClass containingClass = psiMethod.getContainingClass();
        if (containingClass == null) {
            containingClass = (PsiClass) psiMethod.getParent().getContext();
        }

        // follow implementation
        if (PsiUtil.isAbstract(containingClass)) {
            psiMethod.accept(this);
            PsiElement[] psiElements = DefinitionsScopedSearch.search(psiMethod).toArray(PsiElement.EMPTY_ARRAY);
            if (psiElements.length == 1) {
                methodAccept(psiElements[0]);
            } else {
                for (PsiElement psiElement : psiElements) {
                    if (psiElement instanceof PsiMethod) {
                        if (alreadyInStack((PsiMethod) psiElement)) continue;

                        if (!params.isSmartInterface() && params.getInterfaceImplFilter().allow((PsiMethod) psiElement))
                            methodAccept(psiElement);
                    }
                }
            }
        } else {
            // resolve variable initializer
            if (params.isSmartInterface() && !PsiUtil.isExternal(containingClass))
                containingClass.accept(implementationFinder);
            psiMethod.accept(this);
        }
        sequenceDiagram.end();
    }

    private boolean alreadyInStack(PsiMethod psiMethod) {
        MethodDescription method = createMethod(psiMethod);
        return currentStack.isReqursive(method);
    }

    private void methodAccept(PsiElement psiElement) {
        if (psiElement instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) psiElement;
            if (params.getMethodFilter().allow(method)) {
                PsiClass containingClass = (method).getContainingClass();
                if (params.isSmartInterface() && containingClass != null && !PsiUtil.isExternal(containingClass))
                    containingClass.accept(implementationFinder);
                method.accept(this);
            }
        }
    }

    public void visitElement(PsiElement psiElement) {
        psiElement.acceptChildren(this);
    }

    public void visitMethod(PsiMethod psiMethod) {
        MethodDescription method = createMethod(psiMethod);
        sequenceDiagram.addSub(new FunctionNode(method.getClassDescription().getClassShortName(), method.getMethodSignature(), null));
        if (topStack == null) {
            topStack = new CallStack(method);
            currentStack = topStack;
        } else {
            if (!params.isAllowRecursion() && currentStack.isReqursive(method))
                return;
            currentStack = currentStack.methodCall(method);
        }
        super.visitMethod(psiMethod);
    }

    @Override
    public void visitCallExpression(PsiCallExpression callExpression) {
        if (PsiUtil.isPipeline(callExpression)) {
            _exprStack.push(callExpression);
            _callStack.push(currentStack);

            callExpression.getFirstChild().acceptChildren(this);

            if (!_exprStack.isEmpty()) {
                CallStack old = currentStack;
                PsiCallExpression pop = _exprStack.pop();
                currentStack = _callStack.pop();
                findAbstractImplFilter(pop, pop.resolveMethod());
                methodCall(pop.resolveMethod());
                currentStack = old;
            }
            super.visitCallExpression(callExpression);
        } else if (PsiUtil.isComplexCall(callExpression)) {
            _exprStack.push(callExpression);
            _callStack.push(currentStack);
            super.visitCallExpression(callExpression);
            if (!_exprStack.isEmpty()) {
                CallStack old = currentStack;
                PsiCallExpression pop = _exprStack.pop();
                currentStack = _callStack.pop();
                findAbstractImplFilter(pop, pop.resolveMethod());
                methodCall(pop.resolveMethod());
                currentStack = old;
            }
        } else {
            PsiMethod psiMethod = callExpression.resolveMethod();
            findAbstractImplFilter(callExpression, psiMethod);
            methodCall(psiMethod);
            super.visitCallExpression(callExpression);
        }
    }

    /**
     * If the psiMethod's containing class is Interface or abstract, then try to find it's implement class.
     *
     * @param callExpression
     * @param psiMethod
     */
    private void findAbstractImplFilter(PsiCallExpression callExpression, PsiMethod psiMethod) {
        try {
            PsiClass containingClass = psiMethod.getContainingClass();
            if (PsiUtil.isAbstract(containingClass)) {
                String type = containingClass.getQualifiedName();
                String impl = ((PsiMethodCallExpressionImpl) callExpression).getMethodExpression().getQualifierExpression().getType().getCanonicalText();
                if (!impl.startsWith(type))
                    params.getInterfaceImplFilter().put(type, new ImplementClassFilter(impl));
            }
        } catch (Exception e) {
            //ignore
        }
    }

    private void methodCall(PsiMethod psiMethod) {
        if (psiMethod == null)
            return;
        if (!params.getMethodFilter().allow(psiMethod))
            return;
        else if (depth < params.getMaxDepth() - 1) {
            CallStack oldStack = currentStack;
            depth++;
            LOGGER.debug("+ depth = " + depth + " method = " + psiMethod.getName());
            generate(psiMethod);
            depth--;
            LOGGER.debug("- depth = " + depth + " method = " + psiMethod.getName());
            currentStack = oldStack;
        } else
            currentStack.methodCall(createMethod(psiMethod));
    }

    private MethodDescription createMethod(PsiMethod psiMethod) {
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        List argNames = new ArrayList();
        List argTypes = new ArrayList();
        for (int i = 0; i < parameters.length; i++) {
            PsiParameter parameter = parameters[i];
            argNames.add(parameter.getName());
            PsiType psiType = parameter.getType();
            argTypes.add(psiType == null ? null : psiType.getCanonicalText());
        }
        PsiClass containingClass = psiMethod.getContainingClass();
        if (containingClass == null) {
            containingClass = (PsiClass) psiMethod.getParent().getContext();
        }
        List attributes = createAttributes(psiMethod.getModifierList(), PsiUtil.isExternal(containingClass));
        if (psiMethod.isConstructor())
            return MethodDescription.createConstructorDescription(
                    createClassDescription(containingClass),
                    attributes, argNames, argTypes);
        return MethodDescription.createMethodDescription(
                createClassDescription(containingClass),
                attributes, psiMethod.getName(), psiMethod.getReturnType().getCanonicalText(),
                argNames, argTypes);
    }

    private ClassDescription createClassDescription(PsiClass psiClass) {
        return new ClassDescription(psiClass.getQualifiedName(),
                createAttributes(psiClass.getModifierList(), PsiUtil.isExternal(psiClass)));
    }

    private List createAttributes(PsiModifierList psiModifierList, boolean external) {
        if (psiModifierList == null)
            return Collections.EMPTY_LIST;
        List attributes = new ArrayList();
        for (int i = 0; i < Info.RECOGNIZED_METHOD_ATTRIBUTES.length; i++) {
            String attribute = Info.RECOGNIZED_METHOD_ATTRIBUTES[i];
            if (psiModifierList.hasModifierProperty(attribute))
                attributes.add(attribute);
        }
        if (external)
            attributes.add(Info.EXTERNAL_ATTRIBUTE);
        if (PsiUtil.isInterface(psiModifierList))
            attributes.add(Info.INTERFACE_ATTRIBUTE);
        return attributes;
    }

    @Override
    public void visitLocalVariable(PsiLocalVariable variable) {
        PsiJavaCodeReferenceElement referenceElement = variable.getTypeElement().getInnermostComponentReferenceElement();
        if (referenceElement != null) {
            PsiClass psiClass = (PsiClass) referenceElement.resolve();

            if (PsiUtil.isAbstract(psiClass)) {
                String type = variable.getType().getCanonicalText();
                PsiExpression initializer = variable.getInitializer();
                if (initializer instanceof PsiNewExpression) {
                    String impl = initializer.getType().getCanonicalText();
                    if (!type.equals(impl)) {
                        params.getInterfaceImplFilter().put(type, new ImplementClassFilter(impl));
                    }
                }
            }

        }

        super.visitLocalVariable(variable);
    }

    @Override
    public void visitAssignmentExpression(PsiAssignmentExpression expression) {
        PsiExpression re = expression.getRExpression();
        if (params.isSmartInterface() && re instanceof PsiNewExpression) {
            String face = expression.getType().getCanonicalText();
            String impl = expression.getRExpression().getType().getCanonicalText();

            params.getInterfaceImplFilter().put(face, new ImplementClassFilter(impl));

        }
        super.visitAssignmentExpression(expression);
    }

    @Override
    public void visitLambdaExpression(PsiLambdaExpression expression) {
        MethodDescription method = createMethod(expression);
        if (topStack == null) {
            topStack = new CallStack(method);
            currentStack = topStack;
        } else {
            if (!params.isAllowRecursion() && currentStack.isReqursive(method))
                return;
            currentStack = currentStack.methodCall(method);
        }
        super.visitLambdaExpression(expression);
    }

    private MethodDescription createMethod(PsiLambdaExpression expression) {
        PsiParameter[] parameters = expression.getParameterList().getParameters();
        List argNames = new ArrayList();
        List argTypes = new ArrayList();
        for (int i = 0; i < parameters.length; i++) {
            PsiParameter parameter = parameters[i];
            argNames.add(parameter.getName());
            PsiType psiType = parameter.getType();
            argTypes.add(psiType == null ? null : psiType.getCanonicalText());
        }
        String returnType;
        PsiType functionalInterfaceType = expression.getFunctionalInterfaceType();
        if (functionalInterfaceType == null) {
            returnType = null;
        } else {
            returnType = functionalInterfaceType.getCanonicalText();
        }

        PsiMethod psiMethod = PsiUtil.findEncolsedPsiMethod(expression);
        PsiClass containingClass = psiMethod.getContainingClass();
        if (containingClass == null) {
            containingClass = (PsiClass) psiMethod.getParent().getContext();
        }

        return MethodDescription.createLambdaDescription(
                createClassDescription(containingClass), argNames, argTypes, returnType);
    }

    @Override
    public void visitInstanceOfExpression(PsiInstanceOfExpression expression) {
        super.visitInstanceOfExpression(expression);
    }

    public String toDsl() {
        return sequenceDiagram.toDsl();
    }

    private class ImplementationFinder extends JavaElementVisitor {

        @Override
        public void visitClass(PsiClass aClass) {
            for (PsiClass psiClass : aClass.getSupers()) {
                if (!PsiUtil.isExternal(psiClass))
                    psiClass.accept(this);
            }

            if (!PsiUtil.isAbstract(aClass) && !PsiUtil.isExternal(aClass)) {
                super.visitClass(aClass);
            }
        }

        @Override
        public void visitField(PsiField field) {
            PsiTypeElement typeElement = field.getTypeElement();
            if (typeElement != null) {
                PsiJavaCodeReferenceElement referenceElement = typeElement.getInnermostComponentReferenceElement();
                if (referenceElement != null) {
                    PsiClass psiClass = (PsiClass) referenceElement.resolve();
                    if (PsiUtil.isAbstract(psiClass)) {
                        String type = field.getType().getCanonicalText();
                        PsiExpression initializer = field.getInitializer();
                        if (initializer != null && initializer instanceof PsiNewExpression) {
                            String impl = initializer.getType().getCanonicalText();
                            if (!type.equals(impl)) {
                                params.getInterfaceImplFilter().put(type, new ImplementClassFilter(impl));
                            }
                        }
                    }

                }
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
                String face = expression.getType().getCanonicalText();
                String impl = expression.getRExpression().getType().getCanonicalText();

                params.getInterfaceImplFilter().put(face, new ImplementClassFilter(impl));

            }
            super.visitAssignmentExpression(expression);
        }

        public void visitElement(PsiElement psiElement) {
            psiElement.acceptChildren(this);
        }

    }
}

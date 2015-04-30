package org.intellij.sequencer.generator;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import org.intellij.sequencer.diagram.Info;
import org.intellij.sequencer.util.PsiUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SequenceGenerator extends JavaElementVisitor {
    private static final Logger LOGGER = Logger.getInstance(SequenceGenerator.class.getName());

    private CallStack topStack;
    private CallStack currentStack;
    private int depth;
    private SequenceParams params;

    public SequenceGenerator(SequenceParams params) {
        this.params = params;
    }

    public void visitElement(PsiElement psiElement) {
        PsiUtil.acceptChildren(psiElement, this);
    }

    public void visitClass(PsiClass psiClass) {
    }

    public CallStack generate(PsiMethod psiMethod) {
        psiMethod.accept(this);
        return topStack;
    }

    public void visitMethod(PsiMethod psiMethod) {
        MethodDescription method = createMethod(psiMethod);
        if(topStack == null) {
            topStack = new CallStack(method);
            currentStack = topStack;
        } else {
            if(!params.isAllowRecursion() && currentStack.isReqursive(method))
                return;
            currentStack = currentStack.methodCall(method);
        }
        super.visitMethod(psiMethod);
    }

    public void visitNewExpression(PsiNewExpression psiNewExpression) {
        PsiMethod psiMethod = psiNewExpression.resolveConstructor();
        methodCall(psiMethod);
        super.visitNewExpression(psiNewExpression);
    }

    public void visitMethodCallExpression(PsiMethodCallExpression psiMethodCallExpression) {
        PsiMethod psiMethod = psiMethodCallExpression.resolveMethod();
        methodCall(psiMethod);
        super.visitMethodCallExpression(psiMethodCallExpression);
    }

    private void methodCall(PsiMethod psiMethod) {
        if(psiMethod == null)
            return;
        if(!params.getMethodFilter().allow(psiMethod))
            return;
        else if(depth < params.getMaxDepth() - 1) {
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
        for(int i = 0; i < parameters.length; i++) {
            PsiParameter parameter = parameters[i];
            argNames.add(parameter.getName());
            PsiType psiType = parameter.getType();
            argTypes.add(psiType == null ? null : psiType.getCanonicalText());
        }
        List attributes = createAttributes(psiMethod.getModifierList());
        PsiClass containingClass = psiMethod.getContainingClass();
        if (containingClass == null) {
            containingClass = (PsiClass) psiMethod.getParent().getContext();
        }
        if(psiMethod.isConstructor())
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
              createAttributes(psiClass.getModifierList()));
    }

    private List createAttributes(PsiModifierList psiModifierList) {
        if(psiModifierList == null)
            return Collections.EMPTY_LIST;
        List attributes = new ArrayList();
        for(int i = 0; i < Info.RECOGNIZED_METHOD_ATTRIBUTES.length; i++) {
            String attribute = Info.RECOGNIZED_METHOD_ATTRIBUTES[i];
            if(psiModifierList.hasModifierProperty(attribute))
                attributes.add(attribute);
        }
        if(PsiUtil.isInClassFile(psiModifierList) || PsiUtil.isInJarFileSystem(psiModifierList))
            attributes.add(Info.EXTERNAL_ATTRIBUTE);
        return attributes;
    }

    public void visitReferenceExpression(PsiReferenceExpression psiReferenceExpression) {
    }
}

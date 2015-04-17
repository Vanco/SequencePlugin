package org.intellij.sequencer.util;

import com.intellij.psi.*;
import org.intellij.sequencer.generator.filters.MethodFilter;

public class CallFinder extends JavaElementVisitor {
    private int _callsLeft;
    private MethodFilter _methodFilter;
    private PsiMethod _psiMethod;
    private PsiElement _psiElement;

    public CallFinder(int callsLeft, MethodFilter methodFilter, PsiMethod psiMethod) {
        _callsLeft = callsLeft;
        _methodFilter = methodFilter;
        _psiMethod = psiMethod;
    }

    public void visitElement(PsiElement psiElement) {
        PsiUtil.acceptChildren(psiElement, this);
    }

    public void visitReferenceExpression(PsiReferenceExpression psiReferenceExpression) {
    }

    public PsiElement getPsiElement() {
        return _psiElement;
    }

    public void visitNewExpression(PsiNewExpression psiNewExpression) {
        PsiMethod psiMethod = psiNewExpression.resolveConstructor();
        checkCurrentPsiElement(psiMethod, psiNewExpression);
        super.visitNewExpression(psiNewExpression);
    }

    public void visitMethodCallExpression(PsiMethodCallExpression psiMethodCallExpression) {
        PsiMethod psiMethod = psiMethodCallExpression.resolveMethod();
        checkCurrentPsiElement(psiMethod, psiMethodCallExpression);
        super.visitMethodCallExpression(psiMethodCallExpression);
    }

    private void checkCurrentPsiElement(PsiMethod psiMethod, PsiElement psiElement) {
        if(psiMethod == null)
            return;
        if(_methodFilter.allow(psiMethod)) {
            _callsLeft--;
            if(_callsLeft == 0 && psiMethod == _psiMethod) {
                _psiElement = psiElement;
            }
        }
    }
}

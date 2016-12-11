package org.intellij.sequencer.util;

import com.intellij.psi.*;
import com.intellij.util.containers.Stack;
import org.intellij.sequencer.generator.filters.MethodFilter;

public class CallFinder extends JavaElementVisitor {
    private final Stack<PsiCallExpression> _exprStack = new Stack<PsiCallExpression>();

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
        psiElement.acceptChildren(this);
    }

    public void visitReferenceExpression(PsiReferenceExpression psiReferenceExpression) {
        psiReferenceExpression.acceptChildren(this);
    }

    public PsiElement getPsiElement() {
        return _psiElement;
    }

    public void visitCallExpression(PsiCallExpression callExpression) {
        if (!(PsiUtil.isComplexCall(callExpression) || PsiUtil.isPipeline(callExpression))) {
            PsiMethod psiMethod = callExpression.resolveMethod();
            checkCurrentPsiElement(psiMethod, callExpression);
        } else {
            _exprStack.push(callExpression);
        }
        super.visitCallExpression(callExpression);
        if (!_exprStack.isEmpty() && (PsiUtil.isPipeline(callExpression) || PsiUtil.isComplexCall(callExpression))) {
            PsiCallExpression pop = _exprStack.pop();
            checkCurrentPsiElement(pop.resolveMethod(), pop);
        }
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

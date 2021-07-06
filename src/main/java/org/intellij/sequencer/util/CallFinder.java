package org.intellij.sequencer.util;

import com.intellij.psi.*;
import com.intellij.util.containers.Stack;
import org.intellij.sequencer.generator.filters.MethodFilter;

public class CallFinder extends JavaElementVisitor {
    private final Stack<PsiCallExpression> _exprStack = new Stack<>();

    private int _callsLeft;
    private final MethodFilter _methodFilter;
    private final PsiMethod _psiMethod;
    private PsiElement _psiElement;
    private boolean found = false;

    public CallFinder(int callsLeft, MethodFilter methodFilter, PsiMethod psiMethod) {
        _callsLeft = callsLeft;
        _methodFilter = methodFilter;
        _psiMethod = psiMethod;
    }

    public void visitElement(PsiElement psiElement) {
        if (!found)
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
            if (checkCurrentPsiElement(psiMethod, callExpression)) return;
        } else {
            _exprStack.push(callExpression);
        }
        super.visitCallExpression(callExpression);
        if (!found && !_exprStack.isEmpty() && (PsiUtil.isPipeline(callExpression) || PsiUtil.isComplexCall(callExpression))) {
            PsiCallExpression pop = _exprStack.pop();
            checkCurrentPsiElement(pop.resolveMethod(), pop);
        }
    }

    private boolean checkCurrentPsiElement(PsiMethod psiMethod, PsiElement psiElement) {
//        System.out.println("==> check " + _psiMethod.getName() + " with " + psiElement.getText() + "...");
        if (psiMethod == null)
            return false;
        if (_methodFilter.allow(psiMethod)) {
            _callsLeft--;
            if (psiMethod == _psiMethod) {
                System.out.println("found");
                _psiElement = psiElement;
                found = true;
            }

            if (_callsLeft == 0 && !found) {
                _psiElement = psiElement;
            }
        }
        return found;
    }
}

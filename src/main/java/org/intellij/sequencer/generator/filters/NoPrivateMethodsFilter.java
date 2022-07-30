package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.intellij.sequencer.openapi.filters.PsiElementFilter;

/**
 * Exclude private method.
 */
public class NoPrivateMethodsFilter implements PsiElementFilter {
    private boolean _noPrivateMethods = false;

    public NoPrivateMethodsFilter(boolean noPrivateMethods) {
        _noPrivateMethods = noPrivateMethods;
    }

    public boolean isNoPrivateMethods() {
        return _noPrivateMethods;
    }

    public void setNoPrivateMethods(boolean noPrivateMethods) {
        _noPrivateMethods = noPrivateMethods;
    }

    public boolean allow(PsiElement psiElement) {
        if(_noPrivateMethods
                && (psiElement instanceof PsiMethod)
                && isPrivateMethod((PsiMethod) psiElement))
            return false;
        return true;
    }

    private boolean isPrivateMethod(PsiMethod psiMethod) {
        return psiMethod.getModifierList().hasModifierProperty("private");
    }

}

package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.intellij.sequencer.openapi.filters.PsiElementFilter;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoPrivateMethodsFilter that = (NoPrivateMethodsFilter) o;
        return _noPrivateMethods == that._noPrivateMethods;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_noPrivateMethods);
    }


}

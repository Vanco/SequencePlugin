package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.intellij.sequencer.openapi.filters.MethodFilter;

/**
 * Exclude private method.
 */
public class NoPrivateMethodsFilter implements MethodFilter {
    private final boolean _noPrivateMethods;

    public NoPrivateMethodsFilter(boolean noPrivateMethods) {
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
        return o != null && getClass() == o.getClass();
    }

}

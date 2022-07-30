package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.intellij.sequencer.openapi.filters.PsiElementFilter;

/**
 * Exclude constructors.
 */
public class NoConstructorsFilter implements PsiElementFilter {
    private boolean _noConstructors = false;

    public NoConstructorsFilter(boolean noConstructors) {
        _noConstructors = noConstructors;
    }

    public boolean isNoConstructors() {
        return _noConstructors;
    }

    public void setNoConstructors(boolean noConstructors) {
        _noConstructors = noConstructors;
    }

    public boolean allow(PsiElement psiElement) {
        if(_noConstructors
                && (psiElement instanceof PsiMethod)
                && ((PsiMethod) psiElement).isConstructor())
            return false;
        return true;
    }
}

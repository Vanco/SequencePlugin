package org.intellij.sequencer.ext.kotlin.filters;

import com.intellij.psi.PsiElement;
import org.intellij.sequencer.openapi.filters.MethodFilter;
import org.jetbrains.kotlin.psi.KtConstructor;

public class KtNoConstructorsFilter implements MethodFilter {
    private final boolean _noConstructors;

    public KtNoConstructorsFilter(boolean noConstructors) {
        _noConstructors = noConstructors;
    }

    @Override
    public boolean allow(PsiElement psiElement) {
        if(_noConstructors
                && (psiElement instanceof KtConstructor)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

}

package org.intellij.sequencer.ext.kotlin.filters;

import com.intellij.psi.PsiElement;
import org.intellij.sequencer.openapi.filters.MethodFilter;
import org.jetbrains.kotlin.psi.KtFunction;

public class KtNoGetterSetterFilter implements MethodFilter {
    private final boolean _noGetterSetters;

    public KtNoGetterSetterFilter(boolean noGetterSetters) {
        _noGetterSetters = noGetterSetters;
    }

    @Override
    public boolean allow(PsiElement psiElement) {
        if(_noGetterSetters
                && (psiElement instanceof KtFunction)
                && isGetterSetter((KtFunction) psiElement)) {
            return false;
        }
        return true;
    }

    private boolean isGetterSetter(KtFunction psiElement) { //todo kotlin
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

}

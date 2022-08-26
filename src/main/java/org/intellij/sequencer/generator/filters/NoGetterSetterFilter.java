package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PropertyUtil;
import org.intellij.sequencer.openapi.filters.MethodFilter;

/**
 * Exclude getter/setter method.
 */
public class NoGetterSetterFilter implements MethodFilter {
    private final boolean _noGetterSetters;

    public NoGetterSetterFilter(boolean noGetterSetters) {
        _noGetterSetters = noGetterSetters;
    }

    public boolean allow(PsiElement psiElement) {
        if(_noGetterSetters
                && (psiElement instanceof PsiMethod)
                && isGetterSetter((PsiMethod) psiElement))
            return false;
        return true;
    }

    private boolean isGetterSetter(PsiMethod psiMethod) {
        return PropertyUtil.isSimplePropertyGetter(psiMethod) ||
              PropertyUtil.isSimplePropertySetter(psiMethod);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

}

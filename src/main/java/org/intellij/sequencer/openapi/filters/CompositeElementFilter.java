package org.intellij.sequencer.openapi.filters;

import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.List;

public class CompositeElementFilter implements MethodFilter {
    private final List<MethodFilter> _filters = new ArrayList<>();

    public void addFilter(MethodFilter filter) {
        //remove old if exist
        _filters.remove(filter);
        _filters.add(filter);
    }

    public void removeFilter(MethodFilter filter) {
        _filters.remove(filter);
    }

    /**
     *
     * @param psiElement PsiMethod or KtFunction
     * @return false if any filter not allow. true when all filter allowed.
     */
    @Override
    public boolean allow(PsiElement psiElement) {
        for (MethodFilter psiElementFilter : _filters) {
            if (!psiElementFilter.allow(psiElement)) {
                return false;
            }
        }
        return true;
    }

}

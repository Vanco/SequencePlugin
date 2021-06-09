package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.List;

public class CompositeMethodFilter implements MethodFilter {
    private List<MethodFilter> _filters = new ArrayList<>();

    public void addFilter(MethodFilter filter) {
        _filters.add(filter);
    }

    public void removeFilter(MethodFilter filter) {
        _filters.remove(filter);
    }

    @Override
    public boolean allow(PsiMethod psiMethod) {
        for (MethodFilter methodFilter : _filters) {
            if (!methodFilter.allow(psiMethod)) {
                return false;
            }
        }
        return true;
    }

}

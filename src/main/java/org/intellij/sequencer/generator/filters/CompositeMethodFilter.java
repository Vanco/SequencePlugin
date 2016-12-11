package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CompositeMethodFilter implements MethodFilter {
    private List _filters = new ArrayList();

    public void addFilter(MethodFilter filter) {
        _filters.add(filter);
    }

    public void removeFilter(MethodFilter filter) {
        _filters.remove(filter);
    }

    public boolean allow(PsiMethod psiMethod) {
        for(Iterator iterator = _filters.iterator(); iterator.hasNext();) {
            MethodFilter methodFilter = (MethodFilter)iterator.next();
            if(!methodFilter.allow(psiMethod))
                return false;
        }
        return true;
    }

}

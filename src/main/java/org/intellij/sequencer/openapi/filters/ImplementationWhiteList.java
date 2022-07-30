package org.intellij.sequencer.openapi.filters;

import com.intellij.psi.PsiElement;

import java.util.HashMap;

/**
 * The interface should be included.
 *
 * &copy; fanhuagang@gmail.com
 * Created by van on 17/10/2016.
 */
public class ImplementationWhiteList implements PsiElementFilter {
    private final HashMap<String, PsiElementFilter> filters = new HashMap<>();

    public void clear() {
        filters.clear();
    }

    public void put(String key, PsiElementFilter filter) {
        filters.put(key, filter);
    }

    public PsiElementFilter get(String key) {
        return filters.get(key);
    }

    @Override
    public boolean allow(PsiElement psiMethod) {
        for (PsiElementFilter filter : filters.values()) {
            if(filter.allow(psiMethod)) {
                return true;
            }
        }
        return false;
    }
}

package org.intellij.sequencer.openapi.filters;

import com.intellij.psi.PsiElement;

import java.util.HashMap;

/**
 * The interface should be included.
 *
 * &copy; fanhuagang@gmail.com
 * Created by van on 17/10/2016.
 */
public class ImplementationWhiteList implements MethodFilter {
    private final HashMap<String, MethodFilter> filters = new HashMap<>();

    public void clear() {
        filters.clear();
    }

    public void put(String key, MethodFilter filter) {
        filters.put(key, filter);
    }

    public void putIfAbsent(String key, MethodFilter filter) {
        if (!contain(key)) {
            filters.put(key, filter);
        }
    }

    public MethodFilter get(String key) {
        return filters.get(key);
    }

    public boolean contain(String key) {
        return filters.containsKey(key);
    }

    @Override
    public boolean allow(PsiElement psiMethod) {
        for (MethodFilter filter : filters.values()) {
            if(filter.allow(psiMethod)) {
                return true;
            }
        }
        return false;
    }
}

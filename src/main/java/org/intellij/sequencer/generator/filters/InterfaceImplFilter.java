package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiMethod;

import java.util.HashMap;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 17/10/2016.
 */
public class InterfaceImplFilter implements MethodFilter {
    private HashMap<String, MethodFilter> filters = new HashMap<String, MethodFilter>();

    public void clear() {
        filters.clear();
    }

    public void put(String key, MethodFilter filter) {
        filters.put(key, filter);
    }

    public MethodFilter get(String key) {
        return filters.get(key);
    }

    @Override
    public boolean allow(PsiMethod psiMethod) {
        for (MethodFilter filter : filters.values()) {
            if(filter.allow(psiMethod)) {
                return true;
            }
        }
        return false;
    }
}

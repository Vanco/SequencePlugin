package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiMethod;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 18/10/2016.
 */
public class ImplementClassFilter extends SingleClassFilter {
    public ImplementClassFilter(String className) {
        super(className);
    }

    @Override
    public boolean allow(PsiMethod psiMethod) {
        return !super.allow(psiMethod);
    }
}

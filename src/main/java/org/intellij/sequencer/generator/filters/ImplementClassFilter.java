package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiElement;

/**
 * The implement class should be included.
 *
 * &copy; fanhuagang@gmail.com
 * Created by van on 18/10/2016.
 */
public class ImplementClassFilter extends SingleClassFilter {
    public ImplementClassFilter(String className) {
        super(className);
    }

    @Override
    public boolean allow(PsiElement psiElement) {
        return !super.allow(psiElement);
    }
}

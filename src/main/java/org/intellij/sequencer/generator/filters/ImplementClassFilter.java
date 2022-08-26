package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.intellij.sequencer.openapi.filters.MethodFilter;

import java.util.Arrays;

/**
 * The implement class should be included.
 * <p>
 * &copy; fanhuagang@gmail.com
 * Created by van on 18/10/2016.
 */
public class ImplementClassFilter implements MethodFilter {

    private final String[] classNames;

    public ImplementClassFilter(String... className) {
        classNames = className;
    }

    @Override
    public boolean allow(PsiElement psiElement) {
        if (psiElement instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement;
            return psiMethod.getContainingClass() != null
                    && psiMethod.getContainingClass().getQualifiedName() != null
                    && Arrays.stream(classNames).anyMatch(s -> s.equals(psiMethod.getContainingClass().getQualifiedName()));
        }
        return false;
    }
}

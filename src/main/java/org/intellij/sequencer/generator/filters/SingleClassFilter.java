package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.intellij.sequencer.openapi.Constants;
import org.intellij.sequencer.openapi.filters.PsiElementFilter;

import java.util.Objects;

/**
 * The class should be excluded.
 */
public class SingleClassFilter implements PsiElementFilter {
    private final String _className;

    public SingleClassFilter(String className) {
        _className = className;
    }

    public boolean allow(PsiElement psiElement) {
        if (! (psiElement instanceof PsiMethod)) return true;

        PsiMethod psiMethod = (PsiMethod) psiElement;
        if(_className.equals(Constants.ANONYMOUS_CLASS_NAME)
                && (psiMethod.getContainingClass() == null || psiMethod.getContainingClass().getQualifiedName() == null))
            return false;

        if(psiMethod.getContainingClass() != null
                && psiMethod.getContainingClass().getQualifiedName() != null
                && _className.equals(psiMethod.getContainingClass().getQualifiedName()))
            return false;
        else
            return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SingleClassFilter that = (SingleClassFilter) o;
        return _className.equals(that._className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_className);
    }
}

package org.intellij.sequencer.ext.kotlin.filters;

import com.intellij.psi.PsiElement;
import org.intellij.sequencer.openapi.filters.MethodFilter;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtFunction;

import java.util.Objects;

public class KtSingleClassFilter implements MethodFilter {

    private final String _className;

    public KtSingleClassFilter(String className) {
        _className = className;
    }


    @Override
    public boolean allow(PsiElement psiElement) {
        if (psiElement instanceof KtFunction) {
            KtFunction function = (KtFunction) psiElement;

            return function.getFqName() == null || !_className.equals(function.getFqName().parent().asString());

        } else if (psiElement instanceof KtClass) {
            KtClass ktClass = (KtClass) psiElement;

            return ktClass.getFqName() == null || !_className.equals(ktClass.getFqName().asString());
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KtSingleClassFilter that = (KtSingleClassFilter) o;
        return _className.equals(that._className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_className);
    }
}

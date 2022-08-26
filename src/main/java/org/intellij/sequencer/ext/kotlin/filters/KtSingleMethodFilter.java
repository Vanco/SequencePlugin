package org.intellij.sequencer.ext.kotlin.filters;

import com.intellij.psi.PsiElement;
import org.intellij.sequencer.openapi.filters.MethodFilter;

import java.util.List;
import java.util.Objects;

public class KtSingleMethodFilter implements MethodFilter {
    private final String _className;
    private final String _methodName;
    private final List<String> _argTypes;

    public KtSingleMethodFilter(String className, String methodName, List<String> argTypes) {
        _className = className;
        _methodName = methodName;
        _argTypes = argTypes;
    }

    @Override
    public boolean allow(PsiElement psiElement) { //todo kotlin
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KtSingleMethodFilter that = (KtSingleMethodFilter) o;
        return Objects.equals(_className, that._className) && Objects.equals(_methodName, that._methodName) && Objects.equals(_argTypes, that._argTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_className, _methodName, _argTypes);
    }
}

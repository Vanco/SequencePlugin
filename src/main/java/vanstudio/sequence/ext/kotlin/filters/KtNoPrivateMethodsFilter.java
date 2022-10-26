package vanstudio.sequence.ext.kotlin.filters;

import com.intellij.psi.PsiElement;
import vanstudio.sequence.openapi.filters.MethodFilter;

public class KtNoPrivateMethodsFilter implements MethodFilter {
    private final boolean _noPrivateMethods;

    public KtNoPrivateMethodsFilter(boolean noPrivateMethods) {
        _noPrivateMethods = noPrivateMethods;
    }

    @Override
    public boolean allow(PsiElement psiElement) { //todo kotlin
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

}

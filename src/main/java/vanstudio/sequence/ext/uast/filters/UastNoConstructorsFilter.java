package vanstudio.sequence.ext.uast.filters;

import com.intellij.psi.PsiElement;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UastContextKt;
import vanstudio.sequence.openapi.filters.MethodFilter;

public class UastNoConstructorsFilter implements MethodFilter {
    private final boolean _noConstructors;

    public UastNoConstructorsFilter(boolean noConstructors) {
        this._noConstructors = noConstructors;
    }

    @Override
    public boolean allow(PsiElement psiElement) {
        UMethod uMethod = UastContextKt.toUElement(psiElement, UMethod.class);
        return !_noConstructors
                || uMethod == null
                || !uMethod.isConstructor();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return  (o == null || getClass() != o.getClass());
    }

}

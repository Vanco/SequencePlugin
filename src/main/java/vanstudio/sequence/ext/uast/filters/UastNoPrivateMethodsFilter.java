package vanstudio.sequence.ext.uast.filters;

import com.intellij.psi.PsiElement;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UastContextKt;
import org.jetbrains.uast.UastUtils;
import vanstudio.sequence.openapi.filters.MethodFilter;

public class UastNoPrivateMethodsFilter implements MethodFilter {
    private final boolean _noPrivateMethods;

    public UastNoPrivateMethodsFilter(boolean noPrivateMethods) {
        this._noPrivateMethods = noPrivateMethods;
    }

    @Override
    public boolean allow(PsiElement psiElement) {
        UMethod uMethod = UastContextKt.toUElement(psiElement, UMethod.class);

        return !_noPrivateMethods
                || uMethod == null
                || !isPrivateMethod(uMethod);
    }

    private boolean isPrivateMethod(UMethod uMethod) {
        return uMethod.hasModifierProperty("private");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

}

package vanstudio.sequence.ext.uast.filters;

import com.intellij.psi.PsiElement;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UastContextKt;
import vanstudio.sequence.openapi.filters.MethodFilter;

public class UastNoPrivateMethodsFilter implements MethodFilter {
    private final boolean _noPrivateMethods;

    public UastNoPrivateMethodsFilter(boolean noPrivateMethods) {
        this._noPrivateMethods = noPrivateMethods;
    }

    @Override
    public boolean allow(PsiElement psiElement) {
        UElement uElement = UastContextKt.toUElement(psiElement, UElement.class);

        if (_noPrivateMethods
                && uElement instanceof UMethod
                && isPrivateMethod((UMethod) uElement)) return false;
        return true;
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

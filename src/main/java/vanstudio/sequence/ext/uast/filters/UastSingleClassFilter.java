package vanstudio.sequence.ext.uast.filters;

import com.intellij.psi.PsiElement;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UastContextKt;
import vanstudio.sequence.openapi.Constants;
import vanstudio.sequence.openapi.filters.MethodFilter;

import java.util.Objects;

public class UastSingleClassFilter implements MethodFilter {
    private final String _className;

    public UastSingleClassFilter(String className) {
        this._className = className;
    }

    @Override
    public boolean allow(PsiElement psiElement) {
        UElement uElement = UastContextKt.toUElement(psiElement, UElement.class);
        if (uElement instanceof UMethod) {
            UMethod uMethod = (UMethod) uElement;
            if ( /* method in Anonymous class*/
                    _className.equals(Constants.ANONYMOUS_CLASS_NAME)
                            && (uMethod.getContainingClass() == null || uMethod.getContainingClass().getQualifiedName() == null)
                            ||
                            /* or method's classname is same as filtered classname */
                            uMethod.getContainingClass() != null
                                    && uMethod.getContainingClass().getQualifiedName() != null
                                    && _className.equals(uMethod.getContainingClass().getQualifiedName())
            ) return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UastSingleClassFilter that = (UastSingleClassFilter) o;
        return Objects.equals(_className, that._className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_className);
    }
}

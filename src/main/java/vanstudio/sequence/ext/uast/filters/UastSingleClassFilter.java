package vanstudio.sequence.ext.uast.filters;

import com.intellij.psi.PsiElement;
import org.jetbrains.uast.*;
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
        UMethod uMethod = UastContextKt.toUElement(psiElement, UMethod.class);
        if (uMethod != null) {
            UClass uClass = UastUtils.getContainingUClass(uMethod);
            if (
                /* method in Anonymous class*/
                    _className.equals(Constants.ANONYMOUS_CLASS_NAME)
                            && (uClass == null || uClass.getQualifiedName() == null)
                            ||
                            /* or method's classname is same as filtered classname */
                            uClass != null
                                    && uClass.getQualifiedName() != null
                                    && _className.equals(uClass.getQualifiedName()))
                return false;
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

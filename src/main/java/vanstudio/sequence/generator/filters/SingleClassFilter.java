package vanstudio.sequence.generator.filters;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import vanstudio.sequence.openapi.Constants;
import vanstudio.sequence.openapi.filters.MethodFilter;

import java.util.Objects;

/**
 * The class should be excluded.
 */
public class SingleClassFilter implements MethodFilter {
    private final String _className;

    public SingleClassFilter(String className) {
        _className = className;
    }

    public boolean allow(PsiElement psiElement) {
        if (psiElement instanceof PsiMethod) {

            PsiMethod psiMethod = (PsiMethod) psiElement;
            if (
                    /* method in Anonymous class*/
                    _className.equals(Constants.ANONYMOUS_CLASS_NAME)
                    && (psiMethod.getContainingClass() == null || psiMethod.getContainingClass().getQualifiedName() == null)
                    ||
                    /* or method's classname is same as filtered classname */
                    psiMethod.getContainingClass() != null
                    && psiMethod.getContainingClass().getQualifiedName() != null
                    && _className.equals(psiMethod.getContainingClass().getQualifiedName())
            )
                return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SingleClassFilter)) return false;
        SingleClassFilter that = (SingleClassFilter) o;
        return _className.equals(that._className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_className);
    }
}

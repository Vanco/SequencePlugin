package vanstudio.sequence.generator.filters;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import vanstudio.sequence.openapi.filters.MethodFilter;

/**
 * Exclude constructors.
 */
public class NoConstructorsFilter implements MethodFilter {
    private final boolean _noConstructors;

    public NoConstructorsFilter(boolean noConstructors) {
        _noConstructors = noConstructors;
    }

    public boolean allow(PsiElement psiElement) {
        if(_noConstructors
                && (psiElement instanceof PsiMethod)
                && ((PsiMethod) psiElement).isConstructor())
            return false;
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

}

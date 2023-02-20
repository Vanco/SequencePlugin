package vanstudio.sequence.openapi.filters;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLambdaExpression;

import java.util.ArrayList;
import java.util.List;

import vanstudio.sequence.generator.filters.SingleMethodFilter;
import vanstudio.sequence.openapi.model.MethodDescription;

public class CompositeElementFilter implements MethodFilter {
    private final List<MethodFilter> _filters = new ArrayList<>();

    public void addFilter(MethodFilter filter) {
        //remove old if exist
        _filters.remove(filter);
        _filters.add(filter);
    }

    public void removeFilter(MethodFilter filter) {
        _filters.remove(filter);
    }

    /**
     *
     * @param psiElement PsiMethod or KtFunction
     * @return false if any filter not allow. true when all filter allowed.
     */
    @Override
    public boolean allow(PsiElement psiElement) {
        for (MethodFilter psiElementFilter : _filters) {
            if (!psiElementFilter.allow(psiElement)) {
                return false;
            }
        }
        return true;
    }

    public boolean allowLambda(MethodDescription method) {
        for (MethodFilter psiElementFilter : _filters) {
            if (!(psiElementFilter instanceof SingleMethodFilter)) {
                continue;
            }
            SingleMethodFilter singleMethodFilter = (SingleMethodFilter) psiElementFilter;
            if (!singleMethodFilter.allowLambda(method)) {
                return false;
            }
        }
        return true;

    }

}

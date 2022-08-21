package org.intellij.sequencer.ext.kotlin.filters;

import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.psi.PsiElement;
import org.intellij.sequencer.openapi.Constants;
import org.intellij.sequencer.openapi.filters.PsiElementFilter;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtFunction;

import java.util.Objects;

public class KtSingleClassFilter implements PsiElementFilter {

    private final String _className;

    public KtSingleClassFilter(String className) {
        _className = className;
    }


    @Override
    public boolean allow(PsiElement psiElement) {
        if (!(psiElement instanceof KtFunction)) return true;

        KtFunction function = (KtFunction) psiElement;

        if(_className.equals(Constants.ANONYMOUS_CLASS_NAME)) {
            for (PsiSymbolReference ownReference : function.getOwnReferences()) {
                if (ownReference.getElement() instanceof KtClass) {
                   if  (((KtClass) ownReference.getElement()).getName().equals(_className))
                       return false;
                }
            }

            if (function.getParent() == null) return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KtSingleClassFilter that = (KtSingleClassFilter) o;
        return _className.equals(that._className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_className);
    }
}

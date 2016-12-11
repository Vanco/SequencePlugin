package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiMethod;
import org.intellij.sequencer.Constants;

public class SingleClassFilter implements MethodFilter {
    private String _className;

    public SingleClassFilter(String className) {
        _className = className;
    }

    public boolean allow(PsiMethod psiMethod) {
        if(_className.equals(Constants.ANONYMOUS_CLASS_NAME) &&
              psiMethod.getContainingClass().getQualifiedName() == null)
            return false;
        if(psiMethod.getContainingClass().getQualifiedName() == null)
            return true;
        if(_className.equals(psiMethod.getContainingClass().getQualifiedName()))
            return false;
        return true;
    }
}

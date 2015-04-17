package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiMethod;

public class NoConstructorsFilter implements MethodFilter {
    private boolean _noConstructors = false;

    public NoConstructorsFilter(boolean noConstructors) {
        _noConstructors = noConstructors;
    }

    public boolean isNoConstructors() {
        return _noConstructors;
    }

    public void setNoConstructors(boolean noConstructors) {
        _noConstructors = noConstructors;
    }

    public boolean allow(PsiMethod psiMethod) {
        if(_noConstructors && psiMethod.isConstructor())
            return false;
        return true;
    }
}

package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.intellij.sequencer.Constants;
import org.intellij.sequencer.util.PsiUtil;

import java.util.List;

public class SingleMethodFilter implements MethodFilter {
    private String _className;
    private String _methodName;
    private List _argTypes;

    public SingleMethodFilter(String className, String methodName, List argTypes) {
        _className = className;
        _methodName = methodName;
        _argTypes = argTypes;
    }

    public boolean allow(PsiMethod psiMethod) {
        PsiClass containingClass = psiMethod.getContainingClass();
        if(isSameClass(containingClass) && PsiUtil.isMethod(psiMethod, _methodName, _argTypes))
            return false;
        return true;
    }

    private boolean isSameClass(PsiClass containingClass) {
        return _className.equals(Constants.ANONYMOUS_CLASS_NAME) &&
              containingClass.getQualifiedName() == null ||
              _className.equals(containingClass.getQualifiedName());
    }
}

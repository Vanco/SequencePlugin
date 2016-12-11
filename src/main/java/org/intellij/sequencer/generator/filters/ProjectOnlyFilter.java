package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiMethod;
import org.intellij.sequencer.util.PsiUtil;

public class ProjectOnlyFilter implements MethodFilter {
    private boolean _projectClasssesOnly = true;

    public ProjectOnlyFilter(boolean projectClasssesOnly) {
        _projectClasssesOnly = projectClasssesOnly;
    }

    public boolean isProjectClasssesOnly() {
        return _projectClasssesOnly;
    }

    public void setProjectClasssesOnly(boolean projectClasssesOnly) {
        _projectClasssesOnly = projectClasssesOnly;
    }

    public boolean allow(PsiMethod psiMethod) {
        if(_projectClasssesOnly && isInProject(psiMethod))
            return false;
        return true;
    }

    private boolean isInProject(PsiMethod psiMethod) {
        return PsiUtil.isInJarFileSystem(psiMethod) || PsiUtil.isInClassFile(psiMethod);
    }

}

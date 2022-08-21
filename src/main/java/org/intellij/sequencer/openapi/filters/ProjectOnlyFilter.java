package org.intellij.sequencer.openapi.filters;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.intellij.sequencer.openapi.filters.PsiElementFilter;
import org.intellij.sequencer.util.MyPsiUtil;

import java.util.Objects;

/**
 * Only project class/interface should be included.
 */
public class ProjectOnlyFilter implements PsiElementFilter {
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

    public boolean allow(PsiElement psiElement) {
        if(_projectClasssesOnly && isInProject(psiElement))
            return false;
        return true;
    }

    private boolean isInProject(PsiElement psiElement) {
        return MyPsiUtil.isInJarFileSystem(psiElement) || MyPsiUtil.isInClassFile(psiElement);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectOnlyFilter that = (ProjectOnlyFilter) o;
        return _projectClasssesOnly == that._projectClasssesOnly;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_projectClasssesOnly);
    }
}

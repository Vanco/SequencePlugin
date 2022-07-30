package org.intellij.sequencer.openapi.filters;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.intellij.sequencer.openapi.filters.PsiElementFilter;
import org.intellij.sequencer.util.MyPsiUtil;

/**
 * The package should be excluded.
 */
public class PackageFilter implements PsiElementFilter {
    private final String _packageName;
    private final boolean _recursive;

    public PackageFilter(String packageName) {
        this(packageName, false);
    }

    public PackageFilter(String packageName, boolean recursive) {
        _packageName = packageName;
        _recursive = recursive;
    }

    public boolean allow(PsiElement psiElement) {
        String packageName = MyPsiUtil.getPackageName(psiElement);
        if(packageName == null)
            return true;
        if (_recursive) {
            return !packageName.startsWith(_packageName);
        }
        else {
            return !packageName.equals(_packageName);
        }
    }
}

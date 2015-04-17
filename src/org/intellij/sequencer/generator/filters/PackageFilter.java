package org.intellij.sequencer.generator.filters;

import com.intellij.psi.PsiMethod;
import org.intellij.sequencer.util.PsiUtil;

public class PackageFilter implements MethodFilter {
    private String _packageName;
    private boolean _recursive;

    public PackageFilter(String packageName) {
        this(packageName, false);
    }

    public PackageFilter(String packageName, boolean recursive) {
        _packageName = packageName;
        _recursive = recursive;
    }

    public boolean allow(PsiMethod psiMethod) {
        String packageName = PsiUtil.getPackageName(psiMethod);
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

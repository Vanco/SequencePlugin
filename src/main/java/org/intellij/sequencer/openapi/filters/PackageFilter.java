package org.intellij.sequencer.openapi.filters;

import com.intellij.psi.PsiElement;
import org.intellij.sequencer.util.MyPsiUtil;

import java.util.Objects;

/**
 * The package should be excluded.
 */
public class PackageFilter implements MethodFilter {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackageFilter that = (PackageFilter) o;
        return _recursive == that._recursive && _packageName.equals(that._packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_packageName, _recursive);
    }
}

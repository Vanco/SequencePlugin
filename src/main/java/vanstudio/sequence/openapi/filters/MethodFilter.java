package vanstudio.sequence.openapi.filters;

import com.intellij.psi.PsiElement;

public interface MethodFilter {
    boolean allow(PsiElement psiElement);
}

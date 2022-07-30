package org.intellij.sequencer.openapi.filters;

import com.intellij.psi.PsiElement;

public interface PsiElementFilter {
    boolean allow(PsiElement psiElement);
}

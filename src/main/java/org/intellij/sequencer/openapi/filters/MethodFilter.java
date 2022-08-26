package org.intellij.sequencer.openapi.filters;

import com.intellij.psi.PsiElement;

public interface MethodFilter {
    boolean allow(PsiElement psiElement);
}

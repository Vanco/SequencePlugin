package org.intellij.sequencer.openapi;

import com.intellij.psi.PsiElement;
import org.intellij.sequencer.openapi.model.CallStack;
import org.jetbrains.annotations.Nullable;

public interface IGenerator {
    /**
     * Generate <code>CallStack</code> based on <code>PsiElement</code>.
     * @param psiElement instanceof PsiMethod, KtFunction
     * @param parent current CallStack or null if called from UI
     * @return <code>CallStack</code> includes method call of PsiMethod/KtFunction and calls in its body.
     */
    CallStack generate(PsiElement psiElement, @Nullable CallStack parent);
}

package org.intellij.sequencer.generator;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public interface IGenerator {
    /**
     * Generate <code>CallStack</code> based on <code>PsiElement</code>.
     * @param psiElement instanceof PsiMethod, KtFunction
     * @param parent current CallStack or null if called from UI
     * @return <code>CallStack</code> includes method call of FsiMethod/KtFunction and calls in its body.
     */
    CallStack generate(PsiElement psiElement, @Nullable CallStack parent);
}

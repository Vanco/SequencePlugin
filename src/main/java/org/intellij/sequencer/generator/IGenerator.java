package org.intellij.sequencer.generator;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

public interface IGenerator {
    CallStack generate(PsiElement psiElement);
}

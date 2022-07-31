package org.intellij.sequencer.generator;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.intellij.sequencer.openapi.ElementTypeFinder;

public class JavaElementTypeFinder implements ElementTypeFinder {
    @Override
    public <T extends PsiElement> Class<T> findMethod() {
        return (Class<T>) PsiMethod.class;
    }

    @Override
    public <T extends PsiElement> Class<T> findClass() {
        return (Class<T>) PsiClass.class;
    }
}

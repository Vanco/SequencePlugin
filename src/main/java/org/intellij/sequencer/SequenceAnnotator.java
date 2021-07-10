package org.intellij.sequencer;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

public class SequenceAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof PsiIdentifier && element.getParent() instanceof PsiMethod) {
            holder.newAnnotation(HighlightSeverity.INFORMATION, "Generate '"+element.getText()+"' sequence Diagram")
                    .withFix(new SequenceIntentionAction())
                    .create();
        }
    }
}

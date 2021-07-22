package org.intellij.sequencer;

import com.intellij.lang.annotation.Annotation;
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
            Annotation annotation = holder.createAnnotation(new HighlightSeverity("Sequence Diagram", 200), element.getTextRange(), "Generate '" + element.getText() + "' sequence diagram");
            annotation.registerFix(new SequenceIntentionAction());
//                    .range(element)
//                    .highlightType(ProblemHighlightType.INFORMATION)
//                    .create();
        }
    }
}
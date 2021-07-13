package org.intellij.sequencer;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.annotation.ProblemGroup;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import icons.SequencePluginIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SequenceAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof PsiIdentifier && element.getParent() instanceof PsiMethod) {
            holder.newAnnotation(new HighlightSeverity("Sequence Diagram", 200), "Generate '"+element.getText()+"' sequence diagram")
                    .withFix(new SequenceIntentionAction())
                    .range(element)
                    .highlightType(ProblemHighlightType.INFORMATION)
                    .create();
        }
    }
}
package org.intellij.sequencer;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.util.IncorrectOperationException;
import org.intellij.sequencer.generator.SequenceParams;
import org.jetbrains.annotations.NotNull;

import static org.intellij.sequencer.util.ConfigUtil.loadSequenceParams;

public class SequenceIntentionAction extends PsiElementBaseIntentionAction {
    public SequenceIntentionAction() {
    }

    @Override
    public
    @NotNull String getText() {
        return "Generate sequence diagram";
    }

    @Override
    public @NotNull String getFamilyName() {
        return "Sequence diagram";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return element instanceof PsiIdentifier && element.getParent() instanceof PsiMethod;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        ApplicationManager.getApplication().invokeLater(() -> {
            SequenceService plugin = project.getService(SequenceService.class);

            SequenceParams params = loadSequenceParams();

            plugin.showSequence(params, element.getParent());
        });
    }
}

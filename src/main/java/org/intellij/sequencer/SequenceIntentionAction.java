package org.intellij.sequencer;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.intellij.sequencer.config.SequenceParamsState;
import org.intellij.sequencer.generator.SequenceParams;
import org.intellij.sequencer.generator.filters.NoConstructorsFilter;
import org.intellij.sequencer.generator.filters.NoGetterSetterFilter;
import org.intellij.sequencer.generator.filters.NoPrivateMethodsFilter;
import org.intellij.sequencer.generator.filters.ProjectOnlyFilter;
import org.jetbrains.annotations.NotNull;

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
        return "Sequence Diagram";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        ApplicationManager.getApplication().invokeLater(() -> {
            SequenceService plugin = project.getService(SequenceService.class);

            SequenceParamsState state = SequenceParamsState.getInstance();

            SequenceParams params = new SequenceParams();
            params.setMaxDepth(state.callDepth);
            params.setSmartInterface(state.smartInterface);
            params.getMethodFilter().addFilter(new ProjectOnlyFilter(state.projectClassesOnly));
            params.getMethodFilter().addFilter(new NoGetterSetterFilter(state.noGetterSetters));
            params.getMethodFilter().addFilter(new NoPrivateMethodsFilter(state.noPrivateMethods));
            params.getMethodFilter().addFilter(new NoConstructorsFilter(state.noConstructors));

            plugin.showSequence(params, element.getParent());
        });
    }
}

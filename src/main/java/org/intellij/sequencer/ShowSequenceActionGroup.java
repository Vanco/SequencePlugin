package org.intellij.sequencer;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiFile;
import org.intellij.sequencer.openapi.ActionFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShowSequenceActionGroup extends ActionGroup {
    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent e) {
        if (e == null) return new AnAction[0];

        final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) return new AnAction[0];

        /*
          For each PsiElement (PsiMethod/KtFunction) found, invoke {@code SequenceService.showSequence(psiElement)}
         */
        ActionFinder.ActionMenuProcessor processor = (psiElement, project) -> {
            project.getService(SequenceService.class).showSequence(psiElement);
        };

        /*
          Get {@code ActionMenuFinder} by PsiFile's Language and find all PsiMethod/KtFunction with gaven processor.
         */
        return ActionFinder.getInstance(psiFile.getLanguage()).find(psiFile, processor);
    }

}

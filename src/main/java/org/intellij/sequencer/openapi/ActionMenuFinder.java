package org.intellij.sequencer.openapi;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageExtension;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public interface ActionMenuFinder {
    @NotNull
    LanguageExtension<ActionMenuFinder> EP_NAME = new LanguageExtension<>("SequenceDiagram.actionMenuFinder");

    @NotNull
    static ActionMenuFinder getInstance(@NotNull Language language) {
        ActionMenuFinder finder = EP_NAME.forLanguage(language);
        return finder == null ? (p, processor) -> new AnAction[0] : finder;
    }

    AnAction[] find(@NotNull PsiElement element, ActionMenuProcessor processor);

    /**
     * {@code ActionMenuProcess.process} should be called in {@code AnAction.actionPerformed}
     */
    @FunctionalInterface
    interface ActionMenuProcessor {
        void process(@NotNull PsiElement element, @NotNull Project project);
    }
}

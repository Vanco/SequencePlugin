package org.intellij.sequencer.openapi;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageExtension;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public interface ActionFinder {
    @NotNull
    LanguageExtension<ActionFinder> EP_NAME = new LanguageExtension<>("SequenceDiagram.actionFinder");
    ActionFinder EmptyFinder = new ActionFinder() {
        @Override
        public AnAction[] find(@NotNull PsiElement element, ActionMenuProcessor processor) {
            return new AnAction[0];
        }

        @Override
        public Boolean isEnabled(PsiElement psiElement) {
            return false;
        }
    };

    @NotNull
    static ActionFinder getInstance(@NotNull Language language) {
        ActionFinder finder = EP_NAME.forLanguage(language);

        return finder == null ? EmptyFinder : finder;
    }

    AnAction[] find(@NotNull PsiElement element, ActionMenuProcessor processor);

    Boolean isEnabled(PsiElement psiElement);

    /**
     * {@code ActionMenuProcess.process} should be called in {@code AnAction.actionPerformed}
     */
    @FunctionalInterface
    interface ActionMenuProcessor {
        void process(@NotNull PsiElement element, @NotNull Project project);
    }
}

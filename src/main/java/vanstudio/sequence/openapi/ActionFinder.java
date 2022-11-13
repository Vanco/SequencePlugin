package vanstudio.sequence.openapi;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageExtension;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public interface ActionFinder {
    static String[] UAST_Language = new String[] {
          "JAVA", "kotlin", "Groovy", "Scala"
    };
    @NotNull
    LanguageExtension<ActionFinder> EP_NAME = new LanguageExtension<>("SequenceDiagram.actionFinder");
    static boolean isValid(@NotNull Language language) {
        boolean match = Arrays.stream(UAST_Language).anyMatch(p -> p.equals(language.getID()));

        return match || EP_NAME.forLanguage(language) != null;
    }

    @Nullable
    static ActionFinder getInstance(@NotNull Language language) {

        return EP_NAME.forLanguage(language);
    }

    AnAction[] find(@NotNull Project project, @NotNull PsiElement element, Task task);

    /**
     * {@code Task.run} should be called in {@code AnAction.actionPerformed}
     */
    @FunctionalInterface
    interface Task {
        void run(@NotNull PsiElement element, @NotNull Project project);
    }
}

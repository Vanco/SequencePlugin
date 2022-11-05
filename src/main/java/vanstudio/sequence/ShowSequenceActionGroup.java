package vanstudio.sequence;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vanstudio.sequence.openapi.ActionFinder;

public class ShowSequenceActionGroup extends ActionGroup implements DumbAware {

    @Override
    public void update(@NotNull AnActionEvent e) {


        @Nullable PsiElement psiElement = e.getData(CommonDataKeys.PSI_FILE);

        boolean disabled  = psiElement == null
                || ActionFinder.getInstance(psiElement.getLanguage()) == null;

        e.getPresentation().setEnabled(!disabled);

    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        if (e == null) return AnAction.EMPTY_ARRAY;

        Project project = e.getProject();
        if (project == null) return AnAction.EMPTY_ARRAY;

        if (DumbService.isDumb(project)) return AnAction.EMPTY_ARRAY;

        final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) return AnAction.EMPTY_ARRAY;
        /*
          For each PsiElement (PsiMethod/KtFunction) found, invoke {@code SequenceService.showSequence(psiElement)}
         */
        ActionFinder.Task task = (psiElement, myProj) -> myProj.getService(SequenceService.class).showSequence(psiElement);

        /*
          Get {@code ActionMenuFinder} by PsiFile's Language and find all PsiMethod/KtFunction as AnAction with gaven task.
         */
       return ReadAction.compute(() -> {
            ActionFinder actionFinder = ActionFinder.getInstance(psiFile.getLanguage());
            return actionFinder == null ? AnAction.EMPTY_ARRAY : actionFinder.find(project, psiFile, task);
        });
    }

//    @Override
//    public @NotNull ActionUpdateThread getActionUpdateThread() {
//        return ActionUpdateThread.BGT;
//    }
}

package vanstudio.sequence;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.concurrency.NonUrgentExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vanstudio.sequence.openapi.ActionFinder;
import vanstudio.sequence.openapi.ElementTypeFinder;

/**
 * Show Sequence generate options dialog.
 */
public class ShowSequenceAction extends AnAction implements DumbAware {

    public ShowSequenceAction() {
    }

    /**
     * Enable or disable the menu base on file type. Current only java file will enable the menu.
     *
     * @param event event
     */
    public void update(@NotNull AnActionEvent event) {
        super.update(event);

        Presentation presentation = event.getPresentation();

        @Nullable PsiElement psiElement = event.getData(CommonDataKeys.PSI_FILE);
        presentation.setEnabled(isEnabled(psiElement));

    }

    private boolean isEnabled(PsiElement psiElement) {
        return psiElement != null
                && ActionFinder.isValid(psiElement.getLanguage());
    }

    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;

        SequenceService plugin = project.getService(SequenceService.class);

        PsiElement psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
        final PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);

        if (psiElement == null) {
            final Caret caret = event.getData(CommonDataKeys.CARET);

            if (psiFile != null && caret != null) {
                // try to find the enclosed PsiMethod / KtFunction of caret
                ElementTypeFinder typeFinder = ElementTypeFinder.EP_NAME.forLanguage(psiFile.getLanguage());
                if (typeFinder != null) {
                    Class<? extends PsiElement> method = typeFinder.findMethod();
                    psiElement = PsiTreeUtil.findElementOfClassAtOffset(psiFile, caret.getOffset(), method, false);

                    // try to get top PsiClass / KtClass
                    if (psiElement == null) {
                        Class<? extends PsiElement> aClass = typeFinder.findClass();
                        psiElement = PsiTreeUtil.findElementOfClassAtOffset(psiFile, caret.getOffset(), aClass, false);
                        if (psiElement != null) {
                            chooseMethodToGenerate(event, plugin, psiElement, project);
                            return;
                        }
                    }
                }
            }
        }

        if (psiElement != null) {
            plugin.showSequence(psiElement);
        } else {
            if (psiFile != null) {
                chooseMethodToGenerate(event, plugin, psiFile, project);
            }
        }
    }

    private void chooseMethodToGenerate(@NotNull AnActionEvent event, SequenceService plugin, PsiElement psiElement, @NotNull Project project) {

        // for PsiClass, show popup menu list method to choose
//        AnAction[] list;

        /*
          For each PsiElement (PsiMethod/KtFunction) found, invoke {@code SequenceService.showSequence(psiElement)}
         */
        ActionFinder.Task task = (method, myProject) -> plugin.showSequence(method);

        /*
          Get {@code ActionMenuFinder} by PsiFile's Language and find all PsiMethod/KtFunction with gaven task.
         */
//        list = ReadAction.compute(() -> {
//            ActionFinder actionFinder = ActionFinder.getInstance(psiElement.getLanguage());
//            if (actionFinder == null) {
//                return AnAction.EMPTY_ARRAY;
//            } else {
//                return actionFinder.find(project, psiElement, task);
//            }
//        });

        ReadAction.nonBlocking(() -> {
            ActionFinder actionFinder = ActionFinder.getInstance(psiElement.getLanguage());
            if (actionFinder == null) {
                return AnAction.EMPTY_ARRAY;
            } else {
                return actionFinder.find(project, psiElement, task);
            }
        }).inSmartMode(project).finishOnUiThread(ModalityState.defaultModalityState(), list -> {
            ActionGroup actionGroup = new ActionGroup() {
                @Override
                public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
                    return list;
                }
            };

            JBPopupFactory.getInstance().createActionGroupPopup("Choose Method ...", actionGroup, event.getDataContext(),
                    null, false).showInBestPositionFor(event.getDataContext());
        }).submit(NonUrgentExecutor.getInstance());
    }

//    @Override
//    public @NotNull ActionUpdateThread getActionUpdateThread() {
//        return ActionUpdateThread.BGT;
//    }
}

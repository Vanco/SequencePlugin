package org.intellij.sequencer;

import com.intellij.icons.AllIcons;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.sequencer.openapi.ActionMenuFinder;
import org.intellij.sequencer.openapi.ElementTypeFinder;
import org.intellij.sequencer.util.MyPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.psi.KtFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * Show Sequence generate options dialog.
 */
public class ShowSequenceAction extends AnAction {

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

        PsiElement psiElement = event.getData(CommonDataKeys.PSI_FILE);
        presentation.setEnabled(isEnabled(psiElement));

    }

    private boolean isEnabled(PsiElement psiElement) {
        // only JAVA or Kotlin will enable the generator.
        return psiElement != null
                && (psiElement.getLanguage().is(JavaLanguage.INSTANCE)
                || psiElement.getLanguage().is(KotlinLanguage.INSTANCE)
        );
    }

    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;

        SequenceService plugin = project.getService(SequenceService.class);

        PsiElement psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
        if (psiElement == null) {
            // try to find the enclosed PsiMethod / KtFunction of caret
            final PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
            final Caret caret = event.getData(CommonDataKeys.CARET);

            if (psiFile != null && caret != null) {
                Class<? extends PsiElement> method = ElementTypeFinder.EP_NAME.forLanguage(psiFile.getLanguage()).findMethod();
                psiElement = PsiTreeUtil.findElementOfClassAtOffset(psiFile, caret.getOffset(), method, false);
            }

            // try to get top PsiClass / KtClass
            if (psiElement == null && psiFile != null && caret != null) {
                Class<? extends PsiElement> aClass = ElementTypeFinder.EP_NAME.forLanguage(psiFile.getLanguage()).findClass();
                psiElement = PsiTreeUtil.findElementOfClassAtOffset(psiFile, caret.getOffset(), aClass, false);
                chooseMethodToGenerate(event, plugin, Objects.requireNonNullElse(psiElement, psiFile));
            }
        }

        if (psiElement instanceof PsiClass) {
            chooseMethodToGenerate(event, plugin, psiElement);
        } else if (psiElement instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) psiElement;
            plugin.showSequence(method);
        } else if (psiElement instanceof KtFunction) {
            // generate kotlin function
            plugin.showSequence(psiElement);
        }

    }

    private void chooseMethodToGenerate(@NotNull AnActionEvent event, SequenceService plugin, PsiElement psiElement) {

        // for PsiClass, show popup menu list method to choose
        AnAction[] list;

        /*
          For each PsiElement (PsiMethod/KtFunction) found, invoke {@code SequenceService.showSequence(psiElement)}
         */
        ActionMenuFinder.ActionMenuProcessor processor = (method, project) -> {
            plugin.showSequence(method);
        };

        /*
          Get {@code ActionMenuFinder} by PsiFile's Language and find all PsiMethod/KtFunction with gaven processor.
         */
        list = ActionMenuFinder.getInstance(psiElement.getLanguage()).find(psiElement, processor);


        ActionGroup actionGroup = new ActionGroup() {
            @NotNull
            @Override
            public AnAction[] getChildren(@Nullable AnActionEvent e) {
                return list;
            }
        };

        JBPopupFactory.getInstance().createActionGroupPopup("Choose Method ...", actionGroup, event.getDataContext(),
                null, false).showInBestPositionFor(event.getDataContext());
    }

}

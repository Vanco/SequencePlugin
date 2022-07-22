package org.intellij.sequencer;

import com.intellij.icons.AllIcons;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import org.intellij.sequencer.generator.SequenceParams;
import org.intellij.sequencer.util.MyPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.psi.KtFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.intellij.sequencer.util.ConfigUtil.loadSequenceParams;

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

        SequenceParams params = loadSequenceParams();

        PsiElement psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
        if (psiElement == null) {
            // try to find the enclosedMethod of caret (java)
            final PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
            final Caret caret = event.getData(CommonDataKeys.CARET);
            if (psiFile != null && caret != null && psiFile.getLanguage() == JavaLanguage.INSTANCE) {
                psiElement = MyPsiUtil.getEnclosingMethod(psiFile, caret.getOffset());
            }

            // try to get top PsiClass (java)
            if (psiElement == null && psiFile != null && psiFile.getLanguage() == JavaLanguage.INSTANCE) {
                final Collection<PsiClass> psiClassCollection = MyPsiUtil.findChildrenOfType(psiFile, PsiClass.class);
                chooseMethodToGenerate(event, plugin, params, psiClassCollection);
            }
        }

        if (psiElement instanceof PsiClass) {
            ArrayList<PsiClass> list = new ArrayList<>();
            list.add((PsiClass) psiElement);
            chooseMethodToGenerate(event, plugin, params, list);
        } else if (psiElement instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) psiElement;
            plugin.showSequence(params, method);
        } else if (psiElement instanceof KtFunction) {
            // generate kotlin function
            plugin.showSequence(params, psiElement);
        }

    }

    private void chooseMethodToGenerate(@NotNull AnActionEvent event, SequenceService plugin, SequenceParams params, Collection<PsiClass> psiClassCollection) {

        // for PsiClass, show popup menu list method to choose
        ArrayList<AnAction> list = new ArrayList<>();

        if (psiClassCollection.size() > 1) {
            for (PsiClass psiClass : psiClassCollection) {
                ActionGroup group = new ActionGroup(psiClass.getName(), psiClass.getQualifiedName(), AllIcons.Nodes.Class) {
                    @NotNull
                    @Override
                    public AnAction[] getChildren(@Nullable AnActionEvent e) {
                        return getActions(plugin, params, psiClass);
                    }
                };
                group.setPopup(true);
                list.add(group);

            }
        } else {
            for (PsiClass psiClass : psiClassCollection) {
                list.addAll(Arrays.asList(getActions(plugin, params, psiClass)));
            }
        }

        ActionGroup actionGroup = new ActionGroup() {
            @NotNull
            @Override
            public AnAction[] getChildren(@Nullable AnActionEvent e) {
                return list.toArray(new AnAction[0]);
            }
        };

        JBPopupFactory.getInstance().createActionGroupPopup("Choose Method ...", actionGroup, event.getDataContext(),
                null, false ).showInBestPositionFor(event.getDataContext());
    }

    private AnAction[] getActions( SequenceService plugin, SequenceParams params, PsiClass psiClass) {
        PsiMethod[] methods = psiClass.getMethods();
        ArrayList<AnAction> subList = new ArrayList<>();

        for (PsiMethod method : methods) {
            subList.add(new AnAction(method.getName(), "Generate sequence", AllIcons.Nodes.Method) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    Project project = e.getProject();
                    if (project == null) return;
                    plugin.showSequence(params, method);
                }
            });
        }
        return subList.toArray(new AnAction[0]);
    }
}

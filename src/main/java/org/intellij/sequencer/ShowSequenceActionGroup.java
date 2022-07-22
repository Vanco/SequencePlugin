package org.intellij.sequencer;

import com.intellij.icons.AllIcons;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import org.intellij.sequencer.generator.SequenceParams;
import org.intellij.sequencer.util.MyPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinLanguage;

import java.util.ArrayList;
import java.util.Collection;

import static org.intellij.sequencer.util.ConfigUtil.loadSequenceParams;

public class ShowSequenceActionGroup extends ActionGroup {
    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent e) {
        final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) return new AnAction[0];

        final Collection<PsiClass> psiClassCollection = MyPsiUtil.findChildrenOfType(psiFile, PsiClass.class);

        ArrayList<AnAction> list = new ArrayList<>();

        if (psiClassCollection.size() > 1) {

            for (PsiClass psiClass : psiClassCollection) {
                ActionGroup group = new ActionGroup(psiClass.getName(), psiClass.getQualifiedName(), AllIcons.Nodes.Class) {
                    @NotNull
                    @Override
                    public AnAction[] getChildren(@Nullable AnActionEvent e) {
                        return getActions(psiClass);
                    }
                };
                group.setPopup(true);
                list.add(group);

            }
        } else {
            for (PsiClass psiClass: psiClassCollection) {
                return getActions(psiClass);
            }
        }

        return list.toArray(new AnAction[0]);
    }

    @NotNull
    private AnAction[] getActions(PsiClass psiClass) {
        PsiMethod[] methods = psiClass.getMethods();
        ArrayList<AnAction> subList = new ArrayList<>();

        for (PsiMethod method : methods) {
            subList.add(new AnAction(method.getName(), "Generate sequence " + method.getName() , AllIcons.Nodes.Method) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    Project project = e.getProject();
                    if (project == null) return;

                    SequenceService plugin = project.getService(SequenceService.class);

                    SequenceParams params = loadSequenceParams();

                    plugin.showSequence(params, method);
                }
            });
        }
        return subList.toArray(new AnAction[0]);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        @Nullable PsiFile psiElement = e.getData(CommonDataKeys.PSI_FILE);
        e.getPresentation().setEnabled(psiElement != null
                && (psiElement.getLanguage().is(JavaLanguage.INSTANCE)
                || psiElement.getLanguage().is(KotlinLanguage.INSTANCE)
        ));
    }
}

package org.intellij.sequencer.generator;

import com.intellij.icons.AllIcons;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.sequencer.openapi.ActionFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JavaActionFinder implements ActionFinder {

    @Override
    public Boolean isEnabled(PsiElement psiElement) {
        return psiElement != null
                && psiElement.getLanguage().is(JavaLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public AnAction[] find(@NotNull PsiElement element, ActionMenuProcessor processor) {

        if (element instanceof PsiClass) {
            return getActions((PsiClass) element, processor);
        }

        final Collection<PsiClass> psiClassCollection = PsiTreeUtil.findChildrenOfType(element, PsiClass.class);
        psiClassCollection.removeIf(psiClass -> psiClass instanceof PsiTypeParameter);


        ArrayList<AnAction> list = new ArrayList<>();

        if (psiClassCollection.size() > 1) {

            for (PsiClass psiClass : psiClassCollection) {
                ActionGroup group = new ActionGroup(psiClass.getName(), psiClass.getQualifiedName(), AllIcons.Nodes.Class) {
                    @NotNull
                    @Override
                    public AnAction[] getChildren(@Nullable AnActionEvent e) {
                        return getActions(psiClass, processor);
                    }
                };
                group.setPopup(true);
                list.add(group);

            }
        } else {
            for (PsiClass psiClass : psiClassCollection) {
                list.addAll(List.of(getActions(psiClass, processor)));
            }
        }

        return list.toArray(new AnAction[0]);
    }

    private AnAction[] getActions(PsiClass psiClass, ActionMenuProcessor processor) {
        PsiMethod[] methods = psiClass.getMethods();
        ArrayList<AnAction> subList = new ArrayList<>();

        for (PsiMethod method : methods) {
            subList.add(new AnAction(formatMethod(method), "Generate sequence " + method.getName() , AllIcons.Nodes.Method) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    Project project = e.getProject();
                    if (project == null) return;

                    processor.process(method, project);
                }
            });
        }
        return subList.toArray(new AnAction[0]);
    }

    private String formatMethod(PsiMethod method) {
        String s = method.getName();
        PsiParameter[] parameters = method.getParameterList().getParameters();
        if (parameters.length > 0)
            s = s + "(" + Arrays.stream(parameters).map(p -> p.getType().getPresentableText()).collect(Collectors.joining(",")) + ")";
        else
            s += "()";
        if (method.getReturnType() != null)
            s = s + ": " + method.getReturnType().getPresentableText();
        return s;
    }
}

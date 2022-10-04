package org.intellij.sequencer.ext.kotlin;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.sequencer.openapi.ActionFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.psi.KtFunction;
import org.jetbrains.kotlin.psi.KtFunctionLiteral;
import org.jetbrains.kotlin.psi.KtParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class KtActionFinder implements ActionFinder {

    @Override
    public Boolean isEnabled(PsiElement psiElement) {
        return psiElement != null
                &&  psiElement.getLanguage().is(KotlinLanguage.INSTANCE);
    }

    @Override
    public AnAction[] find(@NotNull PsiElement element, ActionMenuProcessor processor) {
        return getActions(element, processor);
    }

    private AnAction[] getActions(PsiElement psiClass, ActionMenuProcessor processor) {
        @NotNull Collection<KtFunction> methods = PsiTreeUtil.findChildrenOfType(psiClass, KtFunction.class);
        methods.removeIf(p -> p instanceof KtFunctionLiteral);

        ArrayList<AnAction> subList = new ArrayList<>();

        for (KtFunction method : methods) {
            subList.add(new AnAction(formatFunction(method), "Generate sequence " + method.getName() , AllIcons.Nodes.Method) {
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

    private String formatFunction(KtFunction method) {
        String s = method.getName();
        List<KtParameter> valueParameters = method.getValueParameters();
        if (valueParameters.size() > 0) {
            s += "(" + valueParameters.stream().map(p -> getType(p.getText())).collect(Collectors.joining(",")) + ")";
        } else {
            s += "()";
        }
        if (method.getTypeReference()!= null) {
            s += ": " + method.getTypeReference().getText();
        } else {
            s += ": Unit";
        }
        return s;
    }

    private String getType(String text) {
        int idx = text.indexOf(':');
        return text.substring(idx + 1).trim();
    }
}

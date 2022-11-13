package vanstudio.sequence.ext.uast;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.*;
import org.jetbrains.uast.visitor.AbstractUastVisitor;
import vanstudio.sequence.openapi.ActionFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UastActionFinder implements ActionFinder {

    @NotNull
    @Override
    public AnAction[] find(@NotNull Project project, @NotNull PsiElement element, Task task) {

        // UAST api will throw `java.lang.UnsupportedOperationException: Method not implemented `
        // when use with compiled code. https://youtrack.jetbrains.com/issue/IDEA-305778
        if (element instanceof PsiCompiledElement) {
            return AnAction.EMPTY_ARRAY;
        }

        ArrayList<AnAction> list = new ArrayList<>();

        try {
            UElement uElement = UastContextKt.toUElement(element);
            if (uElement != null) {
                uElement.accept(new AbstractUastVisitor() {
                    @Override
                    public boolean visitClass(@NotNull UClass node) {
                        list.add(Separator.create(node.getName()));
                        list.addAll(List.of(getActions(node, task)));

                        return false;
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            // UAST api will throw `java.lang.UnsupportedOperationException: Method not implemented `
            // when use with compiled code. https://youtrack.jetbrains.com/issue/IDEA-305778
        }

        return list.toArray(new AnAction[0]);
    }

    private AnAction[] getActions(UClass uClass, Task task) {
        ArrayList<AnAction> subList = new ArrayList<>();

        UMethod[] methods = uClass.getMethods();
        for (UMethod method : methods) {
            subList.add(new AnAction(formatMethod(method), "Generate sequence " + method.getName(), AllIcons.Nodes.Method) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    Project project = e.getProject();
                    if (project == null) return;
                    task.run(method, project);
                }
            });
        }

        return subList.toArray(new AnAction[0]);
    }

    private String formatMethod(UMethod method) {
        StringBuilder s = new StringBuilder(method.getName());
        List<UParameter> parameters = method.getUastParameters();
        if (parameters.size() > 0) {
            s.append("(");
            s.append(parameters.stream().map(p -> p.getType().getPresentableText()).collect(Collectors.joining(",")));
            s.append(")");
        } else {
            s.append("()");
        }
        if (method.getReturnType() != null) {
            s.append(": ").append(method.getReturnType().getPresentableText());
        }
        return s.toString();
    }

}

package org.intellij.sequencer.ui;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import icons.SequencePluginIcons;
import org.intellij.sequencer.SequenceNavigable;
import org.intellij.sequencer.SequencePanel;
import org.intellij.sequencer.SequenceParamsEditor;
import org.intellij.sequencer.SequenceService;
import org.intellij.sequencer.diagram.Parser;
import org.intellij.sequencer.model.MethodDescription;
import org.intellij.sequencer.impl.EmptySequenceNavigable;
import org.intellij.sequencer.impl.JavaSequenceNavigable;
import org.intellij.sequencer.impl.KtSequenceNavigable;
import org.intellij.sequencer.util.MyPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.KotlinLanguage;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.intellij.sequencer.util.ConfigUtil.loadSequenceParams;
import static org.intellij.sequencer.util.MyPsiUtil.getFileChooser;
import static org.intellij.sequencer.util.MyNotifier.notifyError;

public class Welcome {
    private final JPanel myHtmlPanelWrapper;

    public Welcome() {
        DefaultActionGroup actionGroup = new DefaultActionGroup("SequencerActionGroup", false);
        actionGroup.add(new LoadAction());
        actionGroup.add(new SequenceParamsEditor());

        ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar("SequencerToolbar", actionGroup, false);

        myHtmlPanelWrapper = new JPanel(new BorderLayout());
        myHtmlPanelWrapper.add(actionToolbar.getComponent(), BorderLayout.WEST);
        JEditorPane myPanel = new JEditorPane();
        myHtmlPanelWrapper.add(new JScrollPane(myPanel), BorderLayout.CENTER);
        myHtmlPanelWrapper.repaint();

        actionToolbar.setTargetComponent(myPanel);

        String currentHtml = loadWelcome();
        myPanel.setContentType("text/html");
        myPanel.setText(currentHtml);
        myPanel.setEditable(false);
        myPanel.setEnabled(true);
    }

    @NotNull
    private String loadWelcome() {
        String text = "Welcome to use SequenceDiagram Plugin";

        try (InputStream inputStream = Welcome.class.getResourceAsStream("/welcome.html")) {
            if (inputStream != null)
                try (InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                     BufferedReader reader = new BufferedReader(isr)) {
                    text = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return text;
    }

    public JPanel getMainPanel() {
        return myHtmlPanelWrapper;
    }

    private class LoadAction extends AnAction {

        public LoadAction() {
            super("Open Diagram", "Open SequenceDiagram text (.sdt) file", SequencePluginIcons.OPEN_ICON);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            final JFileChooser chooser = getFileChooser();
            int returnVal = chooser.showOpenDialog(myHtmlPanelWrapper);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                String titleName = file.getName();

                MethodDescription method = Parser.peek(file);

                final Project project = e.getProject();

                if (project == null) return;

                SequenceNavigable navigable = new EmptySequenceNavigable();
                PsiMethod psiMethod = null;

                if (method != null) {
                    psiMethod = MyPsiUtil.findPsiMethod(
                            PsiManager.getInstance(project),
                            method.getClassDescription().getClassName(),
                            method.getMethodName(),
                            method.getArgTypes()
                    );

                    if (psiMethod == null) {
                        String content = "Open success! Method source not found, the navigation is disabled.";
                        notifyError(project, content);
                    } else {
                        if (psiMethod.getLanguage().is(JavaLanguage.INSTANCE)) {
                            navigable = new JavaSequenceNavigable(project);
                        } else if (psiMethod.getLanguage().is(KotlinLanguage.INSTANCE)) {
                            navigable = new KtSequenceNavigable(project);
                        }

                        titleName = method.getTitleName();

                        String content = "Open success! The call link may out of date. Regenerate to fix navigation.";
                        notifyError(project, content);

                    }
                }

                SequencePanel sequencePanel = new SequencePanel(
                        project, navigable, psiMethod, loadSequenceParams()
                );

                sequencePanel.setTitleName(titleName);
                sequencePanel.getModel().readFromFile(file);
                ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(SequenceService.PLUGIN_NAME);
                if (toolWindow == null) return;

                ContentManager contentManager = toolWindow.getContentManager();
                final Content content = contentManager.getFactory().createContent(sequencePanel, titleName, false);
                contentManager.addContent(content);
                contentManager.setSelectedContent(content);
            }

        }
    }


}

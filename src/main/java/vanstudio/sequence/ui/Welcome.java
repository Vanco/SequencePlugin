package vanstudio.sequence.ui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.BrowserHyperlinkListener;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.ui.HTMLEditorKitBuilder;
import com.intellij.util.ui.UIUtil;
import icons.SequencePluginIcons;
import org.jetbrains.annotations.NotNull;
import vanstudio.sequence.SequencePanel;
import vanstudio.sequence.SequenceParamsEditor;
import vanstudio.sequence.SequenceService;
import vanstudio.sequence.diagram.Parser;
import vanstudio.sequence.openapi.model.MethodDescription;
import vanstudio.sequence.util.MyPsiUtil;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static vanstudio.sequence.util.MyNotifier.notifyError;
import static vanstudio.sequence.util.MyPsiUtil.getFileChooser;

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
        JTextPane myPanel = new JTextPane();
        myHtmlPanelWrapper.add(new JScrollPane(myPanel), BorderLayout.CENTER);
        myHtmlPanelWrapper.repaint();

        actionToolbar.setTargetComponent(myPanel);

        String currentHtml = loadWelcome();
        myPanel.setEditable(false);

        HTMLEditorKit kit = HTMLEditorKitBuilder.simple();
        myPanel.setEditorKit(kit);
        myPanel.setHighlighter(null);

        Font descriptionFont = UIUtil.getLabelFont(UIUtil.FontSize.SMALL);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body {color:#"+ ColorUtil.toHex(UIUtil.getLabelFontColor(UIUtil.FontColor.BRIGHTER))+"; font-family:"+descriptionFont.getFamily()+"; font-size: "+descriptionFont.getSize()+"pt; margin: 12px;}");
        styleSheet.addRule("h1 {color: #159957;}");
        styleSheet.addRule("h2 {color: #159957;}");
        styleSheet.addRule("pre {font : 10px monaco; background-color: #"+ColorUtil.toHex(UIUtil.getToolTipBackground())+"; padding: 10px}");

        Document doc = kit.createDefaultDocument();
        myPanel.setDocument(doc);
        myPanel.setText(currentHtml);
        // open link
        myPanel.addHyperlinkListener(new BrowserHyperlinkListener());
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

                        titleName = method.getTitleName();

                        String content = "Open success! The call link may out of date. Regenerate to fix navigation.";
                        notifyError(project, content);

                    }
                }

                final PsiMethod theMethod = psiMethod;
                SequencePanel sequencePanel = new SequencePanel(
                        project, theMethod
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

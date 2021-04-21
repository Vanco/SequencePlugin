package org.intellij.sequencer.ui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import icons.SequencePluginIcons;
import org.intellij.sequencer.SequencePanel;
import org.intellij.sequencer.SequenceService;
import org.intellij.sequencer.generator.SequenceParams;
import org.intellij.sequencer.impl.EmptySequenceNavigable;
import org.intellij.sequencer.util.MdUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class Welcome {
    private final JPanel myHtmlPanelWrapper;

    public Welcome() {
        DefaultActionGroup actionGroup = new DefaultActionGroup("SequencerActionGroup", false);
        actionGroup.add(new LoadAction());

        ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar("SequencerToolbar", actionGroup, false);

        myHtmlPanelWrapper = new JPanel(new BorderLayout());
        myHtmlPanelWrapper.add(actionToolbar.getComponent(), BorderLayout.WEST);
        JEditorPane myPanel = new JEditorPane();
        myHtmlPanelWrapper.add(myPanel, BorderLayout.CENTER);
        myHtmlPanelWrapper.repaint();

        String currentHtml = loadWelcome();
        myPanel.setContentType("text/html");
        myPanel.setText(currentHtml);
        myPanel.setEditable(false);
        myPanel.setEnabled(true);
    }

    @NotNull
    private String loadWelcome() {
        String text = "Welcome to use SequenceDiagram Plugin";

        try (InputStream inputStream = Welcome.class.getResourceAsStream("/welcome.md")) {
            if (inputStream != null)
                try (InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                     BufferedReader reader = new BufferedReader(isr)) {
                    text = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String html = MdUtil.generateMarkdownHtml(text);

        return "<html><head></head>" + html + "</html>";
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
            final JFileChooser chooser = new JFileChooser();
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setDialogTitle("Open Diagram");
            chooser.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith("sdt");
                }

                public String getDescription() {
                    return "SequenceDiagram (.sdt) File";
                }
            });
            int returnVal = chooser.showOpenDialog(myHtmlPanelWrapper);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                String _titleName = file.getName();

                SequencePanel sequencePanel = new SequencePanel(
                        new EmptySequenceNavigable(), null, new SequenceParams()
                );

                sequencePanel.getModel().readFromFile(file);
                ToolWindow toolWindow = ToolWindowManager.getInstance(e.getProject()).getToolWindow(SequenceService.PLUGIN_NAME);
                ContentManager contentManager = toolWindow.getContentManager();
                final Content content = contentManager.getFactory().createContent(sequencePanel, _titleName, false);
                contentManager.addContent(content);
                contentManager.setSelectedContent(content);
            }

        }
    }
}

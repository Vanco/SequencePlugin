package org.intellij.sequencer.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.intellij.sequencer.openapi.SequenceNavigable;
import org.intellij.sequencer.SequencePanel;
import org.intellij.sequencer.SequenceService;
import org.intellij.sequencer.openapi.SequenceNavigableFactory;
import org.jetbrains.annotations.NotNull;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 2020/2/23.
 */
public class SequenceServiceImpl implements SequenceService {

    private final Project _project;
    private final ToolWindow _toolWindow;

    public SequenceServiceImpl(Project project) {

        _project = project;
        _toolWindow = ToolWindowManager.getInstance(_project).getToolWindow(PLUGIN_NAME);

    }

    @Override
    public void showSequence(@NotNull PsiElement psiElement) {

        final SequencePanel sequencePanel = new SequencePanel(_project, psiElement);
        final Content content = addSequencePanel(sequencePanel);
        // register callback when generate finished, update the content title.
        sequencePanel.withFinishedListener(content::setDisplayName);

        Runnable postAction = sequencePanel::generate;
        if (_toolWindow.isActive())
            _toolWindow.show(postAction);
        else
            _toolWindow.activate(postAction);
    }

    private Content addSequencePanel(final SequencePanel sequencePanel) {
        ContentManager contentManager = _toolWindow.getContentManager();
        final Content content = contentManager.getFactory().createContent(sequencePanel, sequencePanel.getTitleName(), false);
        contentManager.addContent(content);
        contentManager.setSelectedContent(content);
        return content;
    }

}

package org.intellij.sequencer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.ContentManagerListener;
import org.intellij.sequencer.diagram.ModelTextEvent;
import org.intellij.sequencer.diagram.ModelTextListener;
import org.intellij.sequencer.generator.SequenceParams;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 2020/4/11.
 */
public class SequenceToolWindowsFactory implements ToolWindowFactory {
    @Override
    public boolean isApplicable(@NotNull Project project) {

        return true;
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setToHideOnEmptyContent(true);

        toolWindow.addContentManagerListener(new ContentManagerListener() {
            @Override
            public void contentRemoved(@NotNull ContentManagerEvent event) {
               if (Objects.requireNonNull(event.getContent().getManager()).getContentCount() == 0) {
                   addEmptyContent(project, toolWindow);
               }
            }

        });

        addEmptyContent(project, toolWindow);
    }

    private void addEmptyContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        SequencePanel sequencePanel = new SequencePanel(null,null, new SequenceParams());
        ContentManager contentManager = toolWindow.getContentManager();
        Content emptyDiagram = contentManager.getFactory().createContent(sequencePanel, "Open...", false);
        contentManager.addContent(emptyDiagram);

        sequencePanel.getModel().addModelTextListener(mte -> emptyDiagram.setDisplayName(sequencePanel.getTitleName()));

    }


}

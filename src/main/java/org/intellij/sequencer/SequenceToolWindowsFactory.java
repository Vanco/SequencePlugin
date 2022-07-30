package org.intellij.sequencer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.intellij.sequencer.ui.Welcome;
import org.jetbrains.annotations.NotNull;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 2020/4/11.
 */
public class SequenceToolWindowsFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        addEmptyContent(project, toolWindow);
    }

    private void addEmptyContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        Welcome welcome = new Welcome();
        ContentManager contentManager = toolWindow.getContentManager();
        Content emptyDiagram = contentManager.getFactory().createContent(welcome.getMainPanel(), "Welcome", false);
        emptyDiagram.setCloseable(false);
        contentManager.addContent(emptyDiagram);

    }


}

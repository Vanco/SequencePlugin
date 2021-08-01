package org.intellij.sequencer;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import icons.SequencePluginIcons;
import org.intellij.sequencer.config.SequenceParamsState;
import org.intellij.sequencer.ui.OptionsUI;
import org.jetbrains.annotations.NotNull;

/**
 * Show Sequence generate options dialog.
 */
public class SequenceParamsEditor extends AnAction {

    public SequenceParamsEditor() {
        super("Settings", "Change default settings", SequencePluginIcons.SETTING_ICON);
    }

    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;

        OptionsUI.OptionsDialogWrapper dialogWrapper = new OptionsUI.OptionsDialogWrapper(project);
        dialogWrapper.show();
        if (dialogWrapper.isOK()) {
            SequenceParamsState state = SequenceParamsState.getInstance();

            state.callDepth = dialogWrapper.getCallStackDepth();
            state.projectClassesOnly = dialogWrapper.isProjectClassesOnly();
            state.noGetterSetters = dialogWrapper.isNoGetterSetters();
            state.noPrivateMethods = dialogWrapper.isNoPrivateMethods();
            state.noConstructors = dialogWrapper.isNoConstructors();
//            state.smartInterface = dialogWrapper.isSmartInterface();

            // Notify parameter change.
            state.fireConfigChanged();
        }
    }

}

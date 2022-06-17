package org.intellij.sequencer.util;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import icons.SequencePluginIcons;
import org.intellij.sequencer.SequenceService;
import org.jetbrains.annotations.Nullable;

public class MyNotifier {

    /**
     * Notification user with content.
     * @param project the project
     * @param content the content max in three line, may have html tag as will
     */
    public static void notifyError(@Nullable Project project, String content) {
        NotificationGroup registeredGroup = NotificationGroup.findRegisteredGroup(SequenceService.PLUGIN_NAME);
        if (registeredGroup == null) return;

        registeredGroup
                .createNotification(content, NotificationType.INFORMATION)
                .setTitle(SequenceService.PLUGIN_NAME)
                .setIcon(SequencePluginIcons.SEQUENCE_ICON_13)
                .notify(project);
    }

    /**
     * Notify user with content and action.
     * @param project the project
     * @param content the content max in three line, may have html tag as will
     * @param action the action to be show in link.
     */
    public static void notifyWithAction(@Nullable Project project, String content, AnAction action) {
        NotificationGroup registeredGroup = NotificationGroup.findRegisteredGroup(SequenceService.PLUGIN_NAME);
        if (registeredGroup == null) return;

        registeredGroup
                .createNotification(content, NotificationType.INFORMATION)
                .setTitle(SequenceService.PLUGIN_NAME)
                .setIcon(SequencePluginIcons.SEQUENCE_ICON_13)
                .addAction(action)
                .notify(project);
    }
}

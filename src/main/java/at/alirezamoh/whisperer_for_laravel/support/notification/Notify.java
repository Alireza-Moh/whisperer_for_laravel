package at.alirezamoh.whisperer_for_laravel.support.notification;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

/**
 * Provides a utility method for displaying notifications to the user
 */
public class Notify {

    /**
     * Displays a success notification to the user
     * @param project           The project in which to display the notification
     * @param content           The content of the notification
     */
    public static void notifySuccess(Project project, String content) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("NotificationGroup")
                .createNotification(content, NotificationType.INFORMATION)
                .notify(project);
    }

    /**
     * Displays an error notification to the user
     * @param project           The project in which to display the notification
     * @param content           The content of the notification
     */
    public static void notifyError(Project project, String content) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("NotificationGroup")
            .createNotification(content, NotificationType.ERROR)
            .notify(project);
    }

    /**
     * Displays a warning notification to the user
     * @param project           The project in which to display the notification
     * @param content           The content of the notification
     */
    public static void notifyWarning(Project project, String content) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("NotificationGroup")
            .createNotification(content, NotificationType.WARNING)
            .notify(project);
    }
}

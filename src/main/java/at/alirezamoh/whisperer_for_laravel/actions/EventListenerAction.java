package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.views.EventListenerView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class EventListenerAction extends BaseAction {
    /**
     * Invoked when the action is performed
     * @param anActionEvent The action event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();

        EventListenerView eventListenerView = new EventListenerView(project);

        if (eventListenerView.showAndGet()) {
            this.create(
                eventListenerView.getEventListenerModel(),
                "eventListener.ftl",
                true,
                project
            );
        }
    }
}

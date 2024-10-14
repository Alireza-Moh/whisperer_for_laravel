package at.alirezamoh.idea_whisperer_for_laravel.actions;

import at.alirezamoh.idea_whisperer_for_laravel.actions.views.ControllerView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ControllerAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        ControllerView controllerView = new ControllerView(project);

        if (controllerView.showAndGet()) {
            this.create(
                controllerView.getControllerModel(),
                "controller.ftl",
                true,
                project
            );
        }
    }
}

package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.views.MiddlewareView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class MiddlewareAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        MiddlewareView middlewareView = new MiddlewareView(project);

        if (middlewareView.showAndGet()) {
            this.create(
                middlewareView.getMiddlewareModel(),
                "middleware.ftl",
                true,
                project
            );
        }
    }
}

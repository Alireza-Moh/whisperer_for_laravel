package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.views.EloquentCastView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class EloquentCastAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        EloquentCastView eloquentCastView = new EloquentCastView(project);

        if (eloquentCastView.showAndGet()) {
            this.create(
                eloquentCastView.getEloquentCastModel(),
                "eloquentCast.ftl",
                true,
                project
            );
        }
    }
}

package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.views.EloquentScopeView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class EloquentScopeAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        EloquentScopeView eloquentScopeView = new EloquentScopeView(project);

        if (eloquentScopeView.showAndGet()) {
            this.create(
                eloquentScopeView.getEloquentScopeModel(),
                "eloquentScope.ftl",
                true,
                project
            );
        }
    }
}

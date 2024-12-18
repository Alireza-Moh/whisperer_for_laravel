package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.views.MigrationView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class MigrationAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        MigrationView migrationView = new MigrationView(project);

        if (migrationView.showAndGet()) {
            this.create(
                migrationView.getMigrationModel(),
                "migration.ftl",
                true,
                project
            );
        }
    }
}

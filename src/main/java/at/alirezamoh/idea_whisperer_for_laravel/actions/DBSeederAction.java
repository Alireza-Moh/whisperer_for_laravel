package at.alirezamoh.idea_whisperer_for_laravel.actions;

import at.alirezamoh.idea_whisperer_for_laravel.actions.views.DBSeederView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DBSeederAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        DBSeederView dbSeederView = new DBSeederView(project);

        if (dbSeederView.showAndGet()) {
            this.create(
                dbSeederView.getDBSeederModel(),
                "dbSeeder.ftl",
                true,
                project
            );
        }
    }
}

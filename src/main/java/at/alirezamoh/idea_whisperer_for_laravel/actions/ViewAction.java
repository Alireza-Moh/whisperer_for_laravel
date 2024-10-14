package at.alirezamoh.idea_whisperer_for_laravel.actions;

import at.alirezamoh.idea_whisperer_for_laravel.actions.views.ViewFileView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ViewAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        ViewFileView viewFileView = new ViewFileView(project);

        if (viewFileView.showAndGet()) {
            this.create(
                viewFileView.getViewFileModel(),
                "view.ftl",
                true,
                project
            );
        }
    }
}

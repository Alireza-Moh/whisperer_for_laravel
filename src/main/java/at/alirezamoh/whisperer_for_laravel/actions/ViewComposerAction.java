package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.views.ViewComposerView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ViewComposerAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        ViewComposerView viewComposerView = new ViewComposerView(project);

        if (viewComposerView.showAndGet()) {
            this.create(
                viewComposerView.getViewComposerModel(),
                "viewComposer.ftl",
                true,
                project
            );
        }
    }
}

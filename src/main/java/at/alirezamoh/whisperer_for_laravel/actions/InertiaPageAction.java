package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.views.InertiaPageView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class InertiaPageAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        InertiaPageView inertiaPageView = new InertiaPageView(project);

        if (inertiaPageView.showAndGet()) {
            this.create(
                inertiaPageView.getInertiaPageModel(),
                "inertiaPage.ftl",
                true,
                project
            );
        }
    }
}

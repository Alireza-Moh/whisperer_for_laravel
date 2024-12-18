package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.views.ObserverView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ObserverAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        ObserverView observerView = new ObserverView(project);

        if (observerView.showAndGet()) {
            this.create(
                observerView.getObserverModel(),
                "observer.ftl",
                true,
                project
            );
        }
    }
}

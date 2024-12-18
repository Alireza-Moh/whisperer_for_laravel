package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.views.JobView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class JobAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        JobView jobView = new JobView(project);

        if (jobView.showAndGet()) {
            this.create(
                jobView.getJobModel(),
                "job.ftl",
                true,
                project
            );
        }
    }
}

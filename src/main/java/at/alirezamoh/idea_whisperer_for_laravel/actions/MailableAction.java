package at.alirezamoh.idea_whisperer_for_laravel.actions;

import at.alirezamoh.idea_whisperer_for_laravel.actions.views.MailableView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class MailableAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        MailableView mailableView = new MailableView(project);

        if (mailableView.showAndGet()) {
            this.create(
                mailableView.getMailableModel(),
                "mailable.ftl",
                true,
                project
            );
        }
    }
}

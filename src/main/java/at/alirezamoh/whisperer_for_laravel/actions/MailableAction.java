package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.models.MailableModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.ViewMailableModel;
import at.alirezamoh.whisperer_for_laravel.actions.views.MailableView;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class MailableAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        MailableView mailableView = new MailableView(project);

        if (mailableView.showAndGet()) {
            MailableModel mailableModel = mailableView.getMailableModel();

            this.create(
                mailableModel,
                "mailable.ftl",
                true,
                project
            );

            if (!mailableModel.getViewName().isEmpty() && project != null) {
                ViewMailableModel viewMailableModel = new ViewMailableModel(
                    SettingsState.getInstance(project),
                    mailableModel.getViewName(),
                    mailableModel.getUnformattedModuleFullPath().equals("app") ? "" : mailableModel.getUnformattedModuleFullPath(),
                    mailableModel.getFormattedModuleFullPath()
                );
                this.create(
                    viewMailableModel,
                    "viewMailable.ftl",
                    true,
                    project
                );
            }
        }
    }
}

package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.models.EloquentModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.FormRequestModel;
import at.alirezamoh.whisperer_for_laravel.actions.views.FormRequestView;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.utils.EloquentUtils;
import at.alirezamoh.whisperer_for_laravel.support.notification.Notify;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class FormRequestAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        FormRequestView formRequestView = new FormRequestView(project);

        if (formRequestView.showAndGet()) {
            FormRequestModel formRequestModel = formRequestView.getFormRequestModel();
            addEloquentModel(project, formRequestModel);
            this.create(
                formRequestModel,
                "formRequest.ftl",
                true,
                project
            );
        }
    }

    private void addEloquentModel(Project project, FormRequestModel formRequestView) {
        if (PluginUtils.getPluginVendor(project) == null) {
            Notify.notifyWarning(project, "Could not add eloquent attributes to the Form Request [Run 'Generate Helper Methods']");
        }
        else {
            String[] parts = formRequestView.getModelNamespace().split("\\\\");
            String modelName = parts[parts.length - 1];

            EloquentModel eloquentModel = new EloquentModel(
                SettingsState.getInstance(project),
                modelName,
                formRequestView.getUnformattedModuleFullPath(),
                formRequestView.getFormattedModuleFullPath(),
                EloquentUtils.getFields(modelName, false, project)
            );

            formRequestView.setModel(eloquentModel);
        }

    }
}

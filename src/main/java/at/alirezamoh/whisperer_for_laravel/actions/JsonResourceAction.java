package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.models.EloquentModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.JsonResourceModel;
import at.alirezamoh.whisperer_for_laravel.actions.views.JsonResourceView;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.utils.EloquentUtils;
import at.alirezamoh.whisperer_for_laravel.support.notification.Notify;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class JsonResourceAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        JsonResourceView jsonResourceView = new JsonResourceView(project);

        if (jsonResourceView.showAndGet()) {
            JsonResourceModel jsonResourceModel = jsonResourceView.getJsonResourceModel();
            addEloquentModel(project, jsonResourceModel);
            this.create(
                jsonResourceModel,
                "jsonResource.ftl",
                true,
                project
            );
        }
    }

    private void addEloquentModel(Project project, JsonResourceModel jsonResourceModel) {
        if (PluginUtils.getPluginVendor(project) == null) {
            Notify.notifyWarning(project, "Could not add eloquent attributes to the Json Resource [Run 'Create Helper Methods For Eloquent/DB']");
        }
        else {
            String[] parts = jsonResourceModel.getModelNamespace().split("\\\\");
            String modelName = parts[parts.length - 1];

            EloquentModel eloquentModel = new EloquentModel(
                SettingsState.getInstance(project),
                modelName,
                jsonResourceModel.getUnformattedModuleFullPath(),
                jsonResourceModel.getFormattedModuleFullPath(),
                EloquentUtils.getFields(modelName, jsonResourceModel.isIncludeRelations(), project)
            );

            jsonResourceModel.setModel(eloquentModel);
        }

    }
}

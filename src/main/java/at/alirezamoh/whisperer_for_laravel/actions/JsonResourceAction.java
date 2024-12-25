package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.models.EloquentModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.JsonResourceModel;
import at.alirezamoh.whisperer_for_laravel.actions.views.JsonResourceView;
import at.alirezamoh.whisperer_for_laravel.support.eloquentUtil.EloquentUtil;
import at.alirezamoh.whisperer_for_laravel.support.notification.Notify;
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
        EloquentUtil eloquentUtil = new EloquentUtil(project);

        if (eloquentUtil.getPluginVendor() == null) {
            Notify.notifyWarning(project, "Could not add eloquent attributes to the Json Resource [Run 'Generate Helper Methods']");
        }
        else {
            String[] parts = jsonResourceModel.getModelNamespace().split("\\\\");
            String modelName = parts[parts.length - 1];

            EloquentModel eloquentModel = new EloquentModel(
                eloquentUtil.getSettingsState(),
                modelName,
                jsonResourceModel.getUnformattedModuleFullPath(),
                jsonResourceModel.getFormattedModuleFullPath(),
                eloquentUtil.getFields(modelName, jsonResourceModel.isIncludeRelations())
            );

            jsonResourceModel.setModel(eloquentModel);
        }

    }
}

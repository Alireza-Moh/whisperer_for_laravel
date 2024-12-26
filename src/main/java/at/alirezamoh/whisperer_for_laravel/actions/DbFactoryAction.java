package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.models.DbFactoryModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.EloquentModel;
import at.alirezamoh.whisperer_for_laravel.actions.views.DbFactoryView;
import at.alirezamoh.whisperer_for_laravel.support.eloquentUtil.EloquentUtil;
import at.alirezamoh.whisperer_for_laravel.support.notification.Notify;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DbFactoryAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        DbFactoryView dbFactoryView = new DbFactoryView(project);

        if (dbFactoryView.showAndGet()) {
            DbFactoryModel dbFactoryModel = dbFactoryView.getDbFactoryModel();

            if (!dbFactoryModel.getModelNamespace().isEmpty()) {
                addEloquentModel(project, dbFactoryModel);
            }

            this.create(
                dbFactoryModel,
                "dbFactory.ftl",
                true,
                project
            );
        }
    }

    private void addEloquentModel(Project project, DbFactoryModel dbFactoryModel) {
        EloquentUtil eloquentUtil = new EloquentUtil(project);

        if (eloquentUtil.getPluginVendor() == null) {
            Notify.notifyWarning(project, "Could not add eloquent attributes to the DB Factory [Run 'Generate Helper Methods']");
        }
        else {
            String[] parts = dbFactoryModel.getModelNamespace().split("\\\\");
            String modelName = parts[parts.length - 1];

            EloquentModel eloquentModel = new EloquentModel(
                eloquentUtil.getSettingsState(),
                modelName,
                dbFactoryModel.getUnformattedModuleFullPath(),
                dbFactoryModel.getFormattedModuleFullPath(),
                eloquentUtil.getFields(modelName, false)
            );

            dbFactoryModel.setModel(eloquentModel);
        }

    }
}

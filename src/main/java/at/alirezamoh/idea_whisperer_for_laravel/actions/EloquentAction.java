package at.alirezamoh.idea_whisperer_for_laravel.actions;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.ControllerModel;
import at.alirezamoh.idea_whisperer_for_laravel.actions.models.MigrationModel;
import at.alirezamoh.idea_whisperer_for_laravel.actions.models.codeGenerationHelperModels.EloquentModel;
import at.alirezamoh.idea_whisperer_for_laravel.actions.views.EloquentView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class EloquentAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        EloquentView eloquentView = new EloquentView(project);

        if (eloquentView.showAndGet()) {
            EloquentModel eloquentModel = eloquentView.getEloquentModel();

            this.create(
                eloquentModel,
                "eloquent.ftl",
                true,
                project
            );

            if (eloquentView.withController()) {
                createController(eloquentView, eloquentModel, project);
            }

            if (eloquentView.withMigration()) {
                createMigration(eloquentView, eloquentModel, project);
            }
        }
    }

    private void createController(EloquentView eloquentView, EloquentModel eloquentModel, Project project) {
        ControllerModel controllerModel = new ControllerModel(
            eloquentView.getControllerName(),
            eloquentModel.getUnformattedModuleFullPath(),
            eloquentModel.getFormattedModuleFullPath(),
            eloquentModel.getModuleSrcPath()
        );
        this.create(
            controllerModel,
            "controller.ftl",
            true,
            project
        );
    }

    private void createMigration(EloquentView eloquentView, EloquentModel eloquentModel, Project project) {
        MigrationModel migrationModel = new MigrationModel(
            eloquentView.getMigrationFileName(),
            eloquentModel.getUnformattedModuleFullPath(),
            eloquentModel.getFormattedModuleFullPath(),
            eloquentModel.getModuleSrcPath(),
            eloquentView.getTableName(),
            true,
            !eloquentModel.getFields().isEmpty(),
            true
        );

        migrationModel.setFields(eloquentModel.getFields());
        this.create(
            migrationModel,
            "migration.ftl",
            true,
            project
        );
    }
}

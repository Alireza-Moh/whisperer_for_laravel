package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.models.ControllerModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.DbFactoryModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.MigrationModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.EloquentModel;
import at.alirezamoh.whisperer_for_laravel.actions.views.EloquentView;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.codeGeneration.HelperCodeExecutor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EloquentAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        EloquentView eloquentView = new EloquentView(project);

        if (eloquentView.showAndGet()) {
            EloquentModel eloquentModel = eloquentView.getEloquentModel();
            DbFactoryModel dbFactoryModel = createDbFactory(eloquentView, eloquentModel, project);

            if (dbFactoryModel != null) {
                eloquentModel.setDbFactoryModel(dbFactoryModel);
            }

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

            if (dbFactoryModel != null) {
                createFactory(dbFactoryModel, project);
            }

            generateHelperCode(project);
        }
    }

    /**
     * Creates a controller based on the provided EloquentView and EloquentModel
     *
     * @param eloquentView   The view containing user input for the controller
     * @param eloquentModel  The model containing data for the controller
     * @param project        The current project
     */
    private void createController(EloquentView eloquentView, EloquentModel eloquentModel, Project project) {
        ControllerModel controllerModel = new ControllerModel(
            SettingsState.getInstance(project),
            eloquentView.getControllerName(),
            eloquentModel.getUnformattedModuleFullPath(),
            eloquentModel.getFormattedModuleFullPath()
        );
        this.create(
            controllerModel,
            "controller.ftl",
            true,
            project
        );
    }

    /**
     * Creates a migration file based on the provided EloquentView and EloquentModel
     *
     * @param eloquentView   The view containing user input for the migration
     * @param eloquentModel  The model containing data for the migration
     * @param project        The current project
     */
    private void createMigration(EloquentView eloquentView, EloquentModel eloquentModel, Project project) {
        MigrationModel migrationModel = new MigrationModel(
            SettingsState.getInstance(project),
            eloquentView.getMigrationFileName(),
            eloquentModel.getUnformattedModuleFullPath().equals("app") ? "" : eloquentModel.getUnformattedModuleFullPath(),
            eloquentModel.getFormattedModuleFullPath(),
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

    /**
     * Creates a database factory based on the provided DbFactoryModel
     *
     * @param factoryModel  The model containing data for the factory
     * @param project       The current project
     */
    private void createFactory(DbFactoryModel factoryModel, Project project) {
        this.create(
            factoryModel,
            "dbFactory.ftl",
            true,
            project
        );
    }

    /**
     * Creates a DbFactoryModel if the EloquentView indicates that a factory should be created
     *
     * @param eloquentView   The view containing user input for the factory
     * @param eloquentModel  The model containing data for the factory
     * @param project        The current project
     * @return A DbFactoryModel if the factory should be created, null otherwise
     */
    private @Nullable DbFactoryModel createDbFactory(EloquentView eloquentView, EloquentModel eloquentModel, Project project) {
        if (eloquentView.withFactory()) {
            DbFactoryModel dbFactoryModel = new DbFactoryModel(
                SettingsState.getInstance(project),
                eloquentView.getFactoryName(),
                eloquentModel.getUnformattedModuleFullPath(),
                eloquentModel.getFormattedModuleFullPath(),
                eloquentModel.getNamespace() + "\\" + eloquentModel.getName()
            );

            dbFactoryModel.setModel(eloquentModel);

            return dbFactoryModel;
        }

        return null;
    }

    /**
     * Generates helper code in the background
     *
     * @param project The current project context
     */
    private void generateHelperCode(Project project) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            HelperCodeExecutor helperCodeExecutor = new HelperCodeExecutor(
                project,
                new EmptyProgressIndicator(),
                false
            );

            helperCodeExecutor.execute();
        });
    }
}

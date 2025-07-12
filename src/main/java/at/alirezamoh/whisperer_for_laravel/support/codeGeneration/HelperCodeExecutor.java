package at.alirezamoh.whisperer_for_laravel.support.codeGeneration;

import at.alirezamoh.whisperer_for_laravel.actions.models.BaseModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.codeGenerationHelperModels.LaravelDbBuilder;
import at.alirezamoh.whisperer_for_laravel.actions.models.codeGenerationHelperModels.LaravelModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.codeGenerationHelperModels.LaravelModelGeneration;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Method;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.whisperer_for_laravel.support.TemplateLoader;
import at.alirezamoh.whisperer_for_laravel.support.codeGeneration.vistors.ClassMethodLoader;
import at.alirezamoh.whisperer_for_laravel.support.notification.Notify;
import at.alirezamoh.whisperer_for_laravel.support.utils.DirectoryUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HelperCodeExecutor {
    /**
     * Methods that should be ignored when generating the base query builder code
     */
    String[] ignoreMethods =
        {
            "__call",
            "__construct",
            "afterQuery",
            "applyAfterQueryCallbacks",
            "clone",
            "cursor",
            "cursorPaginate",
            "decrement",
            "delete",
            "find",
            "findOr",
            "get",
            "increment",
            "latest",
            "oldest",
            "orWhereNot",
            "paginate",
            "pluck",
            "simplePaginate",
            "soleValue",
            "update",
            "upsert",
            "value"
        };

    /**
     * The project where the code generation is executed
     */
    private Project project;

    /**
     * The progress indicator to show the progress of the code generation
     */
    private ProgressIndicator indicator;

    /**
     * Whether to show the final message after the code generation is completed
     */
    private boolean showFinalMessage;

    /**
     * The list of models that were generated
     */
    private List<LaravelModel> models = new ArrayList<>();

    public HelperCodeExecutor(Project project, ProgressIndicator indicator, boolean showFinalMessage) {
        this.project = project;
        this.indicator = indicator;
        this.showFinalMessage = showFinalMessage;
    }

    /**
     * Executes the code generation process
     * It checks if the project is dumb, and if so, it waits until it is smart
     * Then it deletes the plugin vendor directory, creates models helper code,
     * and creates base query builder code
     */
    public void execute() {
        if (indicator.isCanceled()) return;

        DumbService dumbService = DumbService.getInstance(project);

        if (dumbService.isDumb()) {
            dumbService.runWhenSmart(this::run);
        }
        else {
            run();
        }
    }

    /**
     * The main method that runs the code generation process
     * It deletes the plugin vendor directory, creates models helper code,
     * and creates base query builder code
     */
    private void run() {
        if (indicator.isCanceled()) return;
        deletePluginVendorDir();

        //TODO. This method should be replaced with a more robust solution
        DumbService.getInstance(project).runReadActionInSmartMode(() -> {
            if (indicator.isCanceled()) return;
            createModelsHelperCode();

            if (indicator.isCanceled()) return;
            createBaseQueryBuilderCode();
        });

        if (showFinalMessage) {
            outputModelCreationResult();
        }
    }

    /**
     * Deletes the plugin vendor directory
     * This is done in a write action to ensure that the directory is deleted safely
     */
    private void deletePluginVendorDir() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            ApplicationManager.getApplication().runWriteAction(() -> {
                PsiDirectory pluginVendor = PluginUtils.getPluginVendor(project);
                if (pluginVendor != null) {
                    try {
                        pluginVendor.delete();
                    } catch (Exception e) {
                        PluginUtils.getLOG().error("Could not delete the plugin vendor directory", e);
                        Notify.notifyError(project, "Could not delete the plugin vendor directory");
                    }
                }
            });
        });
    }

    /**
     * Creates the models helper code by visiting all eloquent models in the project
     * It groups the models by their namespace and creates a LaravelModelGeneration object for each group
     * Then it creates the template files using the TemplateLoader
     */
    private void createModelsHelperCode() {
        indicator.setText("Loading models...");

        MigrationManager migrationManager = new MigrationManager(project);
        List<LaravelModel> allModels = migrationManager.visit();

        if (!allModels.isEmpty()) {
            Map<String, List<LaravelModel>> groupedModels = allModels.stream()
                .collect(Collectors.groupingBy(LaravelModel::getNamespaceName));

            groupedModels.forEach((namespace, modelsInNamespace) -> {
                LaravelModelGeneration generation = new LaravelModelGeneration(namespace, modelsInNamespace, SettingsState.getInstance(project));
                create(generation, "laravelModels.ftl");
            });

            models = allModels;
        }
    }

    /**
     * Creates the base query builder code by loading methods from the Laravel DB Query Builder and Relationships files
     * It uses the ClassMethodLoader to load the methods and then creates a LaravelDbBuilder object
     * Finally, it creates the template file using the TemplateLoader
     */
    private void createBaseQueryBuilderCode() {
        indicator.setText("Loading query builder methods...");

        ClassMethodLoader methodLoader = new ClassMethodLoader(project);
        List<Method> baseQueryBuilderMethods = new ArrayList<>();

        baseQueryBuilderMethods.addAll(
            methodLoader.loadMethods(
                DirectoryUtils.getFileByName(project, ProjectDefaultPaths.LARAVEL_DB_QUERY_BUILDER_PATH),
                ignoreMethods
            )
        );
        baseQueryBuilderMethods.addAll(
            methodLoader.loadMethods(
                DirectoryUtils.getFileByName(project, ProjectDefaultPaths.LARAVEL_DB_QUERY_RELATIONSHIPS_PATH)
            )
        );

        if (!baseQueryBuilderMethods.isEmpty()) {
            create(
                new LaravelDbBuilder(baseQueryBuilderMethods, SettingsState.getInstance(project)),
                "baseDbQueryBuilder.ftl"
            );
        }
    }

    /**
     * Creates a template file using the TemplateLoader
     * It takes a BaseModel object and a template name as parameters
     * The TemplateLoader will process the model and create the template file in the specified destination
     *
     * @param model The model to be processed
     * @param templateName The name of the template to be created
     */
    private void create(BaseModel model, String templateName) {
        TemplateLoader templateProcessor = new TemplateLoader(
            project,
            templateName,
            model,
            false,
            true
        );

        templateProcessor.createTemplateWithDirectory(false);
    }

    /**
     * Outputs the result of the model creation process
     * It shows a success notification and lists all the generated models with their table names
     */
    private void outputModelCreationResult() {
        Notify.notifySuccess(project, "Code generation successful");
        StringBuilder notificationContent = new StringBuilder(models.size() + " Eloquent Models found:\n");

        for (LaravelModel model : models) {
            notificationContent.append(model.getModelName())
                .append(" -> ")
                .append(model.getTableName())
                .append(",\n");
        }

        if (!notificationContent.isEmpty()) {
            notificationContent.setLength(notificationContent.length() - 2);
        }

        Notify.notifySuccess(project, notificationContent.toString());
    }
}

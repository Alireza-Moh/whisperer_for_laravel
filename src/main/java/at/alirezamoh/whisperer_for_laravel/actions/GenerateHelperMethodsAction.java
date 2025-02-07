package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.models.BaseModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.codeGenerationHelperModels.*;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Method;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.whisperer_for_laravel.support.codeGeneration.MigrationManager;
import at.alirezamoh.whisperer_for_laravel.support.codeGeneration.vistors.ClassMethodLoader;
import at.alirezamoh.whisperer_for_laravel.support.utils.DirectoryUtils;
import at.alirezamoh.whisperer_for_laravel.support.notification.Notify;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.support.TemplateLoader;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenerateHelperMethodsAction extends BaseAction {
    private Project project;

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

    private List<LaravelModel> models = new ArrayList<>();

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        project = anActionEvent.getProject();

        if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
            Notify.notifyWarning(project, "Laravel Framework is not installed");
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            new Task.Modal(project, "Generating Helper Methods", true) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    indicator.setIndeterminate(true);
                    try {
                        ApplicationManager.getApplication().invokeAndWait(() -> deletePluginVendorDir());

                        ApplicationManager.getApplication().runReadAction(() -> {
                            createModelsHelperData(indicator);
                            createBaseQueryBuilderMethods(indicator);
                        });

                        outputModelCreationResult();
                    } catch (Exception e) {
                        PluginUtils.getLOG().error("Could not create helper code", e);
                        Notify.notifyError(project, "Could not create helper code");
                    }
                }
            }.queue();
        });
    }

    private void deletePluginVendorDir() {
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
    }

    private void createModelsHelperData(@NotNull ProgressIndicator indicator) {
        indicator.setText("Loading models...");

        MigrationManager migrationManager = new MigrationManager(project);
        List<LaravelModel> m = migrationManager.visit();

        if (!m.isEmpty()) {
            Map<String, List<LaravelModel>> groupedModels = m.stream()
                .collect(Collectors.groupingBy(LaravelModel::getNamespaceName));

            groupedModels.forEach((namespace, modelsInNamespace) -> {
                LaravelModelGeneration generation = new LaravelModelGeneration(namespace, modelsInNamespace, SettingsState.getInstance(project));
                create(generation, "laravelModels.ftl");
            });

            models = m;
        }
    }

    private void createBaseQueryBuilderMethods(@NotNull ProgressIndicator indicator) {
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

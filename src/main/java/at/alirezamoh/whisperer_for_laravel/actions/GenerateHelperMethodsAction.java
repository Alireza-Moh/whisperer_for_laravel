package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.models.BaseModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.codeGenerationHelperModels.*;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Method;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.whisperer_for_laravel.support.codeGeneration.MigrationManager;
import at.alirezamoh.whisperer_for_laravel.support.codeGeneration.vistors.ClassMethodLoader;
import at.alirezamoh.whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.whisperer_for_laravel.support.notification.Notify;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import at.alirezamoh.whisperer_for_laravel.support.template.TemplateLoader;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenerateHelperMethodsAction extends BaseAction {
    private Project project;

    private static final Logger LOG = Logger.getInstance(GenerateHelperMethodsAction.class);

    List<String> ignoreMethods = Arrays.asList(
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
    );


    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        project = anActionEvent.getProject();

        if (FrameworkUtils.isLaravelFrameworkNotInstalled(project)) {
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

                        Notify.notifySuccess(project, "Code generation successful");
                    } catch (Exception e) {
                        LOG.error("Could not create helper code", e);
                        Notify.notifyError(project, "Could not create helper code");
                    }
                }
            }.queue();
        });
    }

    private void deletePluginVendorDir() {
        ApplicationManager.getApplication().runWriteAction(() -> {
            SettingsState settingsState = SettingsState.getInstance(project);
            String path = ProjectDefaultPaths.WHISPERER_FOR_LARAVEL_DIR_PATH;

            if (!settingsState.isLaravelDirectoryEmpty()) {
                path = StrUtils.addSlashes(settingsState.getLaravelDirectoryPath(), false, true) + path;
            }

            PsiDirectory pluginVendor = DirectoryPsiUtil.getDirectory(project, path);

            if (pluginVendor != null) {
                try {
                    pluginVendor.delete();
                } catch (Exception e) {
                    LOG.error("Could not delete the plugin vendor directory", e);
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
        }
    }

    private void createBaseQueryBuilderMethods(@NotNull ProgressIndicator indicator) {
        indicator.setText("Loading query builder methods...");

        ClassMethodLoader methodLoader = new ClassMethodLoader(project);
        List<Method> baseQueryBuilderMethods = new ArrayList<>();

        baseQueryBuilderMethods.addAll(
            methodLoader.loadMethodsWithIgnore(
                DirectoryPsiUtil.getFileByName(project, ProjectDefaultPaths.LARAVEL_DB_QUERY_BUILDER_PATH),
                ignoreMethods
            )
        );
        baseQueryBuilderMethods.addAll(
            methodLoader.loadMethods(
                DirectoryPsiUtil.getFileByName(project, ProjectDefaultPaths.LARAVEL_DB_QUERY_RELATIONSHIPS_PATH)
            )
        );

        if (!baseQueryBuilderMethods.isEmpty()) {
            create(
                new LaravelDbBuilder(baseQueryBuilderMethods, SettingsState.getInstance(project)),
                "baseDbQueryBuilder.ftl"
            );
        }
    }

    protected void create(BaseModel model, String templateName) {
        TemplateLoader templateProcessor = new TemplateLoader(
            project,
            templateName,
            model,
            false,
            true
        );

        templateProcessor.createTemplateWithDirectory(false);
    }
}

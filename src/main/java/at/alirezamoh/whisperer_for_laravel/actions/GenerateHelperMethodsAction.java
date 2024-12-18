package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.models.BaseModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.codeGenerationHelperModels.*;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Method;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.whisperer_for_laravel.support.codeGeneration.MigrationManager;
import at.alirezamoh.whisperer_for_laravel.support.codeGeneration.vistors.ClassMethodLoader;
import at.alirezamoh.whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.whisperer_for_laravel.support.notification.Notify;
import at.alirezamoh.whisperer_for_laravel.support.template.TemplateLoader;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenerateHelperMethodsAction extends BaseAction {
    private Project project;

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
                        ApplicationManager.getApplication().runReadAction(() -> {
                            createModelsHelperData(indicator);
                            createBaseQueryBuilderMethods(indicator);
                        });

                        Notify.notifySuccess(project, "Code generation successful");
                    } catch (Exception e) {
                        Notify.notifyError(project, "Could not create helper code");
                    }
                }
            }.queue();
        });
    }

    private void createModelsHelperData(@NotNull ProgressIndicator indicator) {
        indicator.setText("Loading models...");

        MigrationManager migrationManager = new MigrationManager(project);
        List<LaravelModel> m = migrationManager.visit();

        if (!m.isEmpty()) {
            LaravelModelGeneration g = new LaravelModelGeneration(m);
            create(
                g,
                "laravelModels.ftl"
            );
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
                new LaravelDbBuilder(baseQueryBuilderMethods),
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

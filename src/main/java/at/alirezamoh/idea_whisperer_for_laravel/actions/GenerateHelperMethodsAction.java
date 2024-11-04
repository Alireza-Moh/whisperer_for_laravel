package at.alirezamoh.idea_whisperer_for_laravel.actions;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.BaseModel;
import at.alirezamoh.idea_whisperer_for_laravel.actions.models.codeGenerationHelperModels.*;
import at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables.Method;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.codeGeneration.MigrationManager;
import at.alirezamoh.idea_whisperer_for_laravel.support.codeGeneration.vistors.ClassMethodLoader;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.notification.Notify;
import at.alirezamoh.idea_whisperer_for_laravel.support.template.TemplateLoader;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GenerateHelperMethodsAction extends BaseAction {
    private Project project;

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
                            createFacades(indicator);
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
        LaravelModelGeneration g = new LaravelModelGeneration(m);

        if (!m.isEmpty()) {
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
            methodLoader.loadMethods(
                DirectoryPsiUtil.getFileByName(project, ProjectDefaultPaths.LARAVEL_DB_QUERY_BUILDER_PATH)
            )
        );
        baseQueryBuilderMethods.addAll(
            methodLoader.loadMethods(
                DirectoryPsiUtil.getFileByName(project, ProjectDefaultPaths.LARAVEL_ELOQUENT_QueryRelationships_PATH)
            )
        );

        if (!baseQueryBuilderMethods.isEmpty()) {
            create(
                    new LaravelDbBuilder(baseQueryBuilderMethods),
                    "baseDbQueryBuilder.ftl"
            );
        }
    }

    private void createFacades(@NotNull ProgressIndicator indicator) {
        indicator.setText("Loading facades...");

        ClassMethodLoader methodLoader = new ClassMethodLoader(project);
        List<Facade> facades = new ArrayList<>();
        PsiDirectory facadeDir = DirectoryPsiUtil.getDirectory(project, ProjectDefaultPaths.LARAVEL_FACADES_DIR_PATH);

        if (facadeDir != null) {
            for (PsiFile facadePsiFile : facadeDir.getFiles()) {
                if (facadePsiFile instanceof PhpFile facadePhpFile) {
                    Condition<PhpClass> condition = phpClass -> phpClass.getName().equals(facadePhpFile.getName().replace(".php", ""));
                    PhpClass facadeClass = PhpPsiUtil.findClass(facadePhpFile, condition);
                    List<Method> facadeMethods = methodLoader.loadMethods(facadePhpFile);

                    if (facadeClass != null) {
                        PhpClass facadeSuperClass = facadeClass.getSuperClass();
                        if (facadeSuperClass != null) {
                            facadeMethods.addAll(methodLoader.loadMethods(facadeSuperClass.getContainingFile()));
                        }

                        facades.add(new Facade(facadeClass.getName(), facadeClass.getFQN(), facadeMethods));
                    }
                }
            }
            create(
                new FacadeBuilder(facades),
                "facades.ftl"
            );
        }
    }

    /**
     * Creates a file from a template
     * @param model         The data model for the template.
     * @param templateName  The name of the template file.
     */
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

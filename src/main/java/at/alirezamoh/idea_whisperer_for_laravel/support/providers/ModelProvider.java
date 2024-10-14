package at.alirezamoh.idea_whisperer_for_laravel.support.providers;

import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a list of Laravel models in a project.
 * This class retrieves all PHP files from the "Models" directory and,
 * if the project is module-based, also from the "Models" directory within each module.
 * It then extracts the fully qualified namespace of each model and returns a list of these namespaces.
 */
public class ModelProvider {
    /**
     * List to store the fully qualified namespaces of the models.
     */
    private List<String> models = new ArrayList<>();

    /**
     * The current project.
     */
    private Project project;

    /**
     * The project settings.
     */
    private SettingsState projectSettingState;

    /**
     * @param project             The current project.
     * @param projectSettingState The plugin settings.
     */
    public ModelProvider(Project project, SettingsState projectSettingState) {
        this.project = project;
        this.projectSettingState = projectSettingState;
    }

    /**
     * Returns a list of fully qualified namespaces of Laravel models.
     * @return The list of model namespaces.
     */
    public List<String> getModels() {
        PsiDirectory modelsDir = DirectoryPsiUtil.getDirectory(project, "/app/Models/");

        if (modelsDir != null) {
            for (PsiFile file : modelsDir.getFiles()) {
                if (file instanceof PhpFile) {
                    addModelToList((PhpFile) file);
                }
            }
        }

        if (projectSettingState.isModuleApplication()) {
            searchForModelsInModules();
        }

        return models;
    }

    /**
     * Searches for models within modules in a module-based project.
     */
    private void searchForModelsInModules() {
        String moduleRootPath = projectSettingState.replaceAndSlashes(projectSettingState.getModuleRootDirectoryPath());
        PsiDirectory rootDir = DirectoryPsiUtil.getDirectory(project, moduleRootPath);

        if (rootDir != null) {
            searchInModuleForModels(rootDir);
        }
    }

    /**
     * Searches for models within module
     */
    private void searchInModuleForModels(PsiDirectory rootDir) {
        String moduleSrcDirName = projectSettingState.replaceAndSlashes(projectSettingState.getModuleSrcDirectoryName());

        for (PsiDirectory module : rootDir.getSubdirectories()) {
            PsiDirectory moduleModelsDir = DirectoryPsiUtil.getDirectory(
                project,
                module.getName() + moduleSrcDirName + "/Models"
            );

            if (moduleModelsDir != null) {
                for (PsiFile file : moduleModelsDir.getFiles()) {
                    if (file instanceof PhpFile modelFile) {
                        addModelToList(modelFile);
                    }
                }
            }
        }
    }

    /**
     * Adds a model's fully qualified namespace to the list.
     * @param phpFile The PHP file representing the model.
     */
    private void addModelToList(PhpFile phpFile) {
        VirtualFile virtualFile = phpFile.getVirtualFile();

        String namespace = phpFile.getMainNamespaceName() + "\\" + virtualFile.getNameWithoutExtension();

        models.add(namespace);
    }
}

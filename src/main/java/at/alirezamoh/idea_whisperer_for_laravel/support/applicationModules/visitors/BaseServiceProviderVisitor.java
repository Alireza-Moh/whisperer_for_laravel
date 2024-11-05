package at.alirezamoh.idea_whisperer_for_laravel.support.applicationModules.visitors;

import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;

/**
 * Base class for visitors that inspect service providers in a module-based Laravel application
 */
abstract public class BaseServiceProviderVisitor extends PsiRecursiveElementWalkingVisitor {
    /**
     * The current project
     */
    protected Project project;

    /**
     * The project settings
     */
    protected SettingsState projectSettingState;

    /**
     * The module root directory path
     */
    protected PsiDirectory moduleRootDirectory;

    /**
     * @param project The current project
     */
    public BaseServiceProviderVisitor(Project project) {
        this.project = project;
        this.projectSettingState = SettingsState.getInstance(project);

        String moduleDirectoryRootPath = StrUtil.addSlashes(
            projectSettingState.getModuleRootDirectoryPath(),
            false,
            false
        );;

        if (moduleDirectoryRootPath != null) {
            moduleRootDirectory = DirectoryPsiUtil.getDirectory(
                project,
                moduleDirectoryRootPath
            );
        }
    }
}

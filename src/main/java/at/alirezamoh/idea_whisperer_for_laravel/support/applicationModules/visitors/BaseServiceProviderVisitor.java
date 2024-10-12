package at.alirezamoh.idea_whisperer_for_laravel.support.applicationModules.visitors;

import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import org.jetbrains.annotations.Nullable;

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

    protected PsiDirectory rootDir;

    /**
     * @param project The current project
     */
    public BaseServiceProviderVisitor(Project project) {
        this.project = project;
        this.projectSettingState = SettingsState.getInstance(project);

        rootDir = DirectoryPsiUtil.getDirectory(
            project,
            projectSettingState.getModuleRootDirectoryPath()
        );
    }

    /**
     * Extracts the first parameter from a method
     * @param method method reference
     * @return the second parameter value
     */
    protected String getFirstParameterFromMethod(MethodReference method) {
        String key = "";

        ParameterList parameters = method.getParameterList();
        if (parameters != null) {
            PsiElement namespaceParameter = parameters.getParameter(0);
            if (namespaceParameter != null) {
                key = StrUtil.removeQuotes(namespaceParameter.getText());
            }
        }

        return key;
    }

    /**
     * Extracts the second parameter from a method
     * @param method method reference
     * @return the second parameter value
     */
    protected @Nullable String getSecondParameterFromMethod(MethodReference method) {
        String key = null;

        ParameterList parameters = method.getParameterList();
        if (parameters != null) {
            PsiElement namespaceParameter = parameters.getParameter(1);
            if (namespaceParameter != null) {
                key = StrUtil.removeQuotes(namespaceParameter.getText());
            }
        }

        return key;
    }

    protected @Nullable String getFileName(String str) {
        int lastSlashIndex = str.lastIndexOf('/');
        if (lastSlashIndex == -1 || lastSlashIndex == str.length() - 1) {
            return null;
        }

        return str.substring(lastSlashIndex + 1);
    }
}

package at.alirezamoh.idea_whisperer_for_laravel.support.applicationModules.utils;

import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.applicationModules.visitors.BootstrapFileVisitor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides utility methods for working with module-based Laravel applications
 * This class offers functionalities for finding the bootstrap service provider file
 * and retrieving a list of service providers from it
 */
public class ApplicationModuleUtil {
    /**
     * Finds the bootstrap service provider file in a Laravel project
     * @param project The project to search in
     * @return        The PsiFile representing the bootstrap service provider file, or null if not found
     */
    public static @Nullable PsiFile findBootstrapProviderFile(Project project) {
        return DirectoryPsiUtil.getFileByName(project, ProjectDefaultPaths.LARAVEL_BOOTSTRAP_PROVIDERS_PATH);
    }

    /**
     * Retrieves a list of service providers from the bootstrap service provider file
     * This method locates the bootstrap service provider file and uses a visitor to extract
     * the registered service providers
     * @param project The project to search in
     * @return A list of PsiFile objects representing the service providers
     */
    public static List<PsiFile> getProviders(Project project) {
        List<PsiFile> providers = new ArrayList<>();
        PsiFile bootstrapProviderFile = findBootstrapProviderFile(project);

        if (bootstrapProviderFile != null) {
            BootstrapFileVisitor providerVisitor = new BootstrapFileVisitor();
            bootstrapProviderFile.acceptChildren(providerVisitor);

            providers = providerVisitor.getProviders();
        }

        return providers;
    }
}

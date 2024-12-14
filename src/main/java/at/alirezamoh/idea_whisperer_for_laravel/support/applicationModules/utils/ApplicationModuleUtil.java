package at.alirezamoh.idea_whisperer_for_laravel.support.applicationModules.utils;

import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.applicationModules.visitors.BootstrapFileVisitor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
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
     * Retrieves a list of service providers from the bootstrap service provider class
     * This method locates the bootstrap service provider file and uses a visitor to extract
     * the registered service providers
     * @param moduleDir Module directory
     * @return A list of PsiFile objects representing the service providers
     */
    public static Collection<PhpClass> getProviders(PsiDirectory moduleDir) {
        List<PhpClass> serviceProviderClasses = new ArrayList<>();
        collectPhpClassesFromDirectory(moduleDir, serviceProviderClasses);

        return serviceProviderClasses;
    }

    /**
     * Recursively collect all PHP classes within a directory
     *
     * @param directory          The VirtualFile directory to search in
     * @param collectedClasses   A list to collect found PhpClass objects
     */
    private static void collectPhpClassesFromDirectory(@NotNull PsiDirectory directory, @NotNull List<PhpClass> collectedClasses) {
        for (PsiFile file : directory.getFiles()) {
            if (file.isDirectory()) {
                collectPhpClassesFromDirectory((PsiDirectory) file, collectedClasses);
            } else if (file instanceof PhpFile phpFile) {
                for (PhpNamedElement element : phpFile.getTopLevelDefs().values()) {
                    if (element instanceof PhpClass serviceProvider && !serviceProvider.isAbstract()) {
                        String serviceProviderFQN = "\\Illuminate\\Support\\ServiceProvider";
                        PhpClass superClass = serviceProvider.getSuperClass();

                        if (superClass != null && serviceProviderFQN.equals(superClass.getFQN())) {
                            collectedClasses.add(serviceProvider);
                        }
                    }
                }
            }
        }
    }
}

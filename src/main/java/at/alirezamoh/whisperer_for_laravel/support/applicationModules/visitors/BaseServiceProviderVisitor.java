package at.alirezamoh.whisperer_for_laravel.support.applicationModules.visitors;

import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.LaravelPaths;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base class for visitors that inspect service providers in a module-based Laravel application
 */
abstract public class BaseServiceProviderVisitor extends PsiRecursiveElementWalkingVisitor {
    /**
     * The current project
     */
    protected Project project;

    /**
     * @param project The current project
     */
    public BaseServiceProviderVisitor(Project project) {
        this.project = project;
    }

    /**
     * Retrieves a list of service providers from the bootstrap service provider class
     * This method locates the bootstrap service provider file and uses a visitor to extract
     * the registered service providers
     *
     * @param project project
     * @return A list of PsiFile objects representing the service providers
     */
    public static Collection<PhpClass> getProviders(Project project) {
        List<PhpClass> serviceProviderClasses = new ArrayList<>();

        PhpClass baseServiceProvider = getBaseServiceProvider(project);
        if (baseServiceProvider != null) {
            PhpIndex phpIndex = PhpIndex.getInstance(project);
            collectPhpClassesFromDirectory(baseServiceProvider.getFQN(), phpIndex, serviceProviderClasses);
        }

        return serviceProviderClasses;
    }

    /**
     * Recursively collect all PHP classes within a directory
     *
     * @param collectedClasses   A list to collect found php classes
     */
    private static void collectPhpClassesFromDirectory(String classFQN, PhpIndex phpIndex, @NotNull List<PhpClass> collectedClasses) {
        Collection<PhpClass> subclasses = phpIndex.getDirectSubclasses(classFQN);

        for (PhpClass subclass : subclasses) {
            if (subclass.isAbstract()) {
                collectPhpClassesFromDirectory(subclass.getFQN(), phpIndex, collectedClasses);
            } else {
                collectedClasses.add(subclass);
            }
        }
    }

    private static @Nullable PhpClass getBaseServiceProvider(Project project) {
        return PhpIndex.getInstance(project)
            .getClassesByFQN(LaravelPaths.LaravelClasses.ServiceProvider)
            .stream()
            .findFirst()
            .orElse(null);
    }
}

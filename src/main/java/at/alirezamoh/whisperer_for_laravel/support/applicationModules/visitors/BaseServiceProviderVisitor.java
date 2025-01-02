package at.alirezamoh.whisperer_for_laravel.support.applicationModules.visitors;

import at.alirezamoh.whisperer_for_laravel.indexes.ServiceProviderIndex;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * Retrieves a list of service providers
     *
     * @param project project
     * @return A list of PsiFiles representing the service providers
     */
    public static Collection<PsiFile> getProviders(Project project) {
        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
        PsiManager psiManager = PsiManager.getInstance(project);

        Collection<String> allKeys = fileBasedIndex.getAllKeys(ServiceProviderIndex.INDEX_ID, project);

        return allKeys.stream()
            .flatMap(key -> fileBasedIndex.getContainingFiles(
                ServiceProviderIndex.INDEX_ID,
                key,
                GlobalSearchScope.allScope(project)
            ).stream())
            .map(psiManager::findFile)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }
}

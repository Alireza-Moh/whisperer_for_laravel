package at.alirezamoh.whisperer_for_laravel.support.caching;

import at.alirezamoh.whisperer_for_laravel.eloquent.ModelRelatedFilesCollector;
import at.alirezamoh.whisperer_for_laravel.support.utils.EloquentUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A project-level service that caches the mapping between Eloquent model names and their corresponding listener files.
 * This avoids traversing service providers and PSI trees on every request.
 */
@Service(Service.Level.PROJECT)
public final class ListenersCacheManager {
    /**
     * Cached mapping from model name to a list of listener PsiFiles.
     */
    private CachedValue<Map<String, List<PsiFile>>> listenersMapping;

    /**
     * Base eloquent model
     */
    private PhpClass baseEloquentModel;

    /**
     * Retrieves the project-level instance of ListenersCacheManager.
     *
     * @param project the current project
     * @return the ListenersCacheManager instance for the given project
     */
    public static ListenersCacheManager getInstance(@NotNull Project project) {
        return project.getService(ListenersCacheManager.class);
    }

    public ListenersCacheManager(Project project) {
        buildCache(project);
        this.baseEloquentModel = EloquentUtils.getEloquentBaseModel(project);
    }

    /**
     * Returns a list of listener files associated with the specified Eloquent model.
     *
     * @param modelName the name of the model
     * @return a list of {@code PsiFile} instances representing the listeners, or an empty list if none found
     */
    public List<PsiFile> getListenersForModel(String modelName) {
        Map<String, List<PsiFile>> mapping = listenersMapping.getValue();
        return mapping.getOrDefault(modelName, Collections.emptyList());
    }

    /**
     * Builds the cached mapping between model names and listener files.
     * This cache is automatically invalidated on any PSI modification.
     *
     * @param project the current project
     */
    private void buildCache(Project project) {
        listenersMapping = CachedValuesManager.getManager(project).createCachedValue(() -> {

            Map<String, List<PsiFile>> mapping = new HashMap<>(extractModelEventsFromProvider(project, "\\App\\Providers\\EventServiceProvider"));
            mapping.putAll(extractModelEventsFromProvider(project, "\\App\\Providers\\AppServiceProvider"));

            return CachedValueProvider.Result.create(mapping, PsiModificationTracker.MODIFICATION_COUNT);
        }, false);
    }

    /**
     * Extract events from a service provider by scanning for "observe" method calls
     *
     * @param project     the current project
     * @param providerFQN the fully qualified name of the provider
     */
    private Map<String, List<PsiFile>> extractModelEventsFromProvider(Project project, String providerFQN) {
        Map<String, List<PsiFile>> mapping = new HashMap<>();
        PhpClass serviceProvider = PhpClassUtils.getClassByFQN(project, providerFQN);

        if (serviceProvider != null && baseEloquentModel != null) {
            serviceProvider.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof MethodReference methodReference && "observe".equals(methodReference.getName())) {
                        List<PhpClassImpl> resolvedClasses = MethodUtils.resolveMethodClasses(methodReference, project);
                        for (PhpClassImpl clazz : resolvedClasses) {
                            if (PhpClassUtils.isChildOf(clazz, baseEloquentModel)) {
                                PsiElement parameter = methodReference.getParameter(0);
                                PsiFile containingFile = ModelRelatedFilesCollector.getContainingFileFromClassConstant(parameter);

                                if (containingFile != null) {
                                    mapping.computeIfAbsent(clazz.getName(), k -> new ArrayList<>()).add(containingFile);
                                }
                            }
                        }
                    }
                    super.visitElement(element);
                }
            });
        }

        return mapping;
    }
}

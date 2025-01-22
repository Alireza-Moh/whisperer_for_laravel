package at.alirezamoh.whisperer_for_laravel.blade.viewName;

import at.alirezamoh.whisperer_for_laravel.blade.viewName.visitors.BladeFileCollector;
import at.alirezamoh.whisperer_for_laravel.indexes.ServiceProviderIndex;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.whisperer_for_laravel.support.utils.DirectoryUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides references to blade files within a Laravel project
 */
public class BladeReference extends PsiReferenceBase<PsiElement> {
    /**
     * The current project
     */
    private Project project;

    /**
     * @param element        The PSI element being referenced
     * @param rangeInElement The text range of the reference within the element
     */
    public BladeReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);

        this.project = element.getProject();
    }

    /**
     * Resolves the reference to the corresponding blade file
     * @return The resolved PSI element (Blade file) or null if not found
     */
    @Override
    public @Nullable PsiElement resolve() {
        String viewPath = StrUtils.removeQuotes(myElement.getText());

        if (viewPath.contains("::")) {
            return searchInServiceProviderForBladeFile(viewPath);
        }

        String[] parts = viewPath.split("\\.");
        PsiDirectory baseDirectory = getBaseViewDirectory();

        if (baseDirectory == null) {
            return null;
        }

        return searchInViewBaseDirForBladeFile(baseDirectory, parts);
    }

    /**
     * Returns an array of variants (code completion suggestions) for the reference
     * @return An array of LookupElementBuilder
     */
    @Override
    public Object @NotNull [] getVariants() {
        BladeFileCollector bladeFileCollector = new BladeFileCollector(project);

        return bladeFileCollector.startSearching().getVariants().toArray();
    }

    /**
     * Gets the base directory for views, considering project settings.
     *
     * @return The base view directory or null if not found.
     */
    private @Nullable PsiDirectory getBaseViewDirectory() {
        return DirectoryUtils.getDirectory(
            project,
            ProjectDefaultPaths.VIEW_PATH
        );
    }

    private PsiElement searchInServiceProviderForBladeFile(String viewPath) {
        AtomicReference<PsiElement> foundedElement = new AtomicReference<>();
        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();

        fileBasedIndex.processAllKeys(ServiceProviderIndex.INDEX_ID, key -> {
            fileBasedIndex.processValues(
                ServiceProviderIndex.INDEX_ID,
                key,
                null,
                (file, serviceProvider) -> {
                    for (Map.Entry<String, String> entry : serviceProvider.getBladeFiles().entrySet()) {

                        if (entry.getKey().equals(viewPath)) {
                            VirtualFile virtualFile = PsiElementUtils.resolveFilePath(entry.getValue());
                            if (virtualFile != null) {
                                foundedElement.set(PsiElementUtils.resolvePsiFile(virtualFile, project));
                                return false;
                            }
                        }
                    }
                    return true;
                },
                GlobalSearchScope.allScope(project)
            );

            return foundedElement.get() == null;
        }, project);

        return foundedElement.get();
    }

    /**
     * Resolves the Blade file using the path components.
     *
     * @param baseDirectory The base directory to start the search
     * @param parts         The parts of the view path
     * @return The resolved Blade file or null if not found
     */
    private @Nullable PsiElement searchInViewBaseDirForBladeFile(PsiDirectory baseDirectory, String[] parts) {
        if (parts.length <= 1) {
            return baseDirectory.findFile(parts[0] + ".blade.php");
        }

        PsiDirectory currentDirectory = baseDirectory;

        for (int i = 0; i < parts.length - 1; i++) {
            currentDirectory = currentDirectory.findSubdirectory(parts[i]);
            if (currentDirectory == null) {
                return null;
            }
        }

        String fileName = parts[parts.length - 1] + ".blade.php";

        return currentDirectory.findFile(fileName);
    }
}

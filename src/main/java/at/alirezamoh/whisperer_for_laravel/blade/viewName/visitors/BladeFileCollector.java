package at.alirezamoh.whisperer_for_laravel.blade.viewName.visitors;

import at.alirezamoh.whisperer_for_laravel.indexes.ServiceProviderIndex;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.whisperer_for_laravel.support.utils.DirectoryUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.blade.BladeFileType;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

import java.util.*;

public class BladeFileCollector {
    /**
     * List of the blade files
     */
    private List<LookupElementBuilder> variants = new ArrayList<>();

    /**
     * The current project
     */
    private Project project;

    /**
     * The project settings
     */
    private SettingsState projectSettingState;

    /**
     * Should save the blade file psiFile
     */
    private boolean withPsiFile;

    public BladeFileCollector(Project project) {
        this.project = project;
        this.projectSettingState = SettingsState.getInstance(project);
    }

    public BladeFileCollector startSearching() {
        String defaultViewPath = ProjectDefaultPaths.VIEW_PATH;
        if (!projectSettingState.isLaravelDirectoryEmpty()) {
            defaultViewPath = StrUtils.addSlashes(
                projectSettingState.getLaravelDirectoryPath(),
                false,
                true
            ) + ProjectDefaultPaths.VIEW_PATH;
        }

        PsiDirectory defaultResDir = DirectoryUtils.getDirectory(project, defaultViewPath);

        if (defaultResDir != null) {
            collectBladeFiles(defaultResDir, "", null, (viewName, filePath) -> {
                variants.add(PsiElementUtils.buildSimpleLookupElement(viewName));
            });
        }

        getBladeFilesFormServiceProviders();

        return this;
    }

    public List<LookupElementBuilder> getVariants() {
        return variants;
    }

    /**
     * Recursively processes Blade files within a directory
     *
     * @param directory     The directory to search in
     * @param currentPath   The current path being traversed
     * @param viewNamespace The view namespace to prepend to the file name
     * @param processor     A callback to handle each found Blade file
     */
    public static void collectBladeFiles(
        PsiDirectory directory,
        String currentPath,
        @Nullable String viewNamespace,
        BiConsumer<String, String> processor
    ) {
        for (PsiFile file : directory.getFiles()) {
            if (file.getFileType() instanceof BladeFileType) {
                String fileName = file.getName().replace(".blade.php", "");
                String finalFileName = currentPath.isEmpty() ? fileName : currentPath + "." + fileName;

                if (viewNamespace != null && !viewNamespace.isEmpty()) {
                    finalFileName = viewNamespace + "::" + finalFileName;
                }

                processor.accept(finalFileName, file.getVirtualFile().getPath());
            }
        }

        for (PsiDirectory subdirectory : directory.getSubdirectories()) {
            String newPath = currentPath.isEmpty()
                ? subdirectory.getName()
                : currentPath + "." + subdirectory.getName();

            collectBladeFiles(subdirectory, newPath, viewNamespace, processor);
        }
    }

    /**
     * Searches for bladeFiles within module
     */
    private void getBladeFilesFormServiceProviders() {
        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();

        fileBasedIndex.processAllKeys(ServiceProviderIndex.INDEX_ID, serviceProviderKey -> {
            fileBasedIndex.processValues(
                ServiceProviderIndex.INDEX_ID,
                serviceProviderKey,
                null,
                (file, serviceProvider) -> {
                    if (serviceProvider == null) return true;

                    for (Map.Entry<String, String> entry : serviceProvider.getBladeFiles().entrySet()) {
                        variants.add(
                            PsiElementUtils.buildSimpleLookupElement(entry.getKey())
                        );
                    }

                    return true;
                },
                GlobalSearchScope.allScope(project)
            );

            return true;
        }, project);
    }
}

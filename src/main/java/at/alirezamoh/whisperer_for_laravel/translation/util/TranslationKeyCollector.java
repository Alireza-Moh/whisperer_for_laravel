package at.alirezamoh.whisperer_for_laravel.translation.util;

import at.alirezamoh.whisperer_for_laravel.indexes.ServiceProviderIndex;
import at.alirezamoh.whisperer_for_laravel.support.WhispererForLaravelIcon;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.IdFilter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TranslationKeyCollector {
    public static TranslationKeyCollector INSTANCE = new TranslationKeyCollector();

    /**
     * Collects translation keys from the project
     *
     * @param project the current project
     * @return a list of LookupElementBuilder containing translation keys
     */
    public List<LookupElementBuilder> collectKeys(@NotNull Project project) {
        List<LookupElementBuilder> variants = new ArrayList<>();
        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();

        TranslationUtil.getKeysFromTranslationIndex(project, fileBasedIndex, variants);
        getKeysFromServiceProviderIndex(project, fileBasedIndex, variants);

        return variants;
    }

    /**
     * Recursively processes Blade files within a directory
     *
     * @param directory The directory to search in
     */
    public static List<PsiFile> collectTranslationFilesFromDir(PsiDirectory directory) {
        List<PsiFile> files = new ArrayList<>();
        Collections.addAll(files, directory.getFiles());

        for (PsiDirectory subdirectory : directory.getSubdirectories()) {
            List<PsiFile> subFiles = collectTranslationFilesFromDir(subdirectory);
            files.addAll(subFiles);
        }

        return files;
    }

    /**
     * Collects keys from the ServiceProviderIndex and adds them to the provided list of variants
     *
     * @param project the current project
     * @param fileBasedIndex php file index
     * @param variants the list to store collected keys
     */
    private void getKeysFromServiceProviderIndex(@NotNull Project project, FileBasedIndex fileBasedIndex, List<LookupElementBuilder> variants) {
        fileBasedIndex.processAllKeys(ServiceProviderIndex.INDEX_ID, serviceProviderKey -> {
                fileBasedIndex.processValues(
                    ServiceProviderIndex.INDEX_ID,
                    serviceProviderKey,
                    null,
                    (file, serviceProvider) -> {
                        if (serviceProvider == null) return true;

                        for (Map.Entry<String, String> entry : serviceProvider.getTranslationKeys().entrySet()) {
                            variants.add(
                                buildLookupElement(entry.getKey(),  buildKeyValue(entry.getValue()))
                            );
                        }

                        return true;
                    },
                    GlobalSearchScope.projectScope(project),
                    IdFilter.getProjectIdFilter(project, true)
                );

                return true;
            },
            GlobalSearchScope.projectScope(project),
            IdFilter.getProjectIdFilter(project, true)
        );
    }

    /**
     * Builds a key-value string for display in the lookup element
     *
     * @param value the value to be displayed
     * @return a formatted string containing the value or an empty string if the value is null or empty
     */
    private @NotNull String buildKeyValue(String value) {
        return (value == null || value.isEmpty())
            ? ""
            : " = " + value.substring(0, value.indexOf("|")).trim();
    }

    /**
     * Builds a LookupElementBuilder for a given key and value
     *
     * @param key the translation key
     * @param value the value associated with the key
     * @return a LookupElementBuilder with the specified key and value
     */
    private LookupElementBuilder buildLookupElement(String key, String value) {
        return LookupElementBuilder
            .create(key)
            .withLookupString(key)
            .withPresentableText(key)
            .withTailText(value, true)
            .bold()
            .withIcon(WhispererForLaravelIcon.LARAVEL_ICON);
    }
}

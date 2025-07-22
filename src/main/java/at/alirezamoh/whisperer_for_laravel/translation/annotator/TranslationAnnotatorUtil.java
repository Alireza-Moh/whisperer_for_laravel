package at.alirezamoh.whisperer_for_laravel.translation.annotator;

import at.alirezamoh.whisperer_for_laravel.indexes.ServiceProviderIndex;
import at.alirezamoh.whisperer_for_laravel.indexes.TranslationIndex;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

final public class TranslationAnnotatorUtil {
    private TranslationAnnotatorUtil() {}

    /**
     * Check if the translation key exists.
     *
     * @param psiElement The psi element for the translation key
     * @param project   The current project
     * @return true or false
     */
    public static boolean doesTranslationKeyNotExists(PsiElement psiElement, Project project) {
        String cleanedTranslationKey = StrUtils.removeQuotes(psiElement.getText());

        if (cleanedTranslationKey.contains("::")) {
            return !checkIfTranslationKeyExistsInServiceProvider(cleanedTranslationKey, project);
        }
        return !checkIfTranslationKeyExists(cleanedTranslationKey, project);
    }

    /**
     * Checks if the translation key exists in the index
     *
     * @param translationKey The translation key to check
     * @param project        The current project
     * @return true if the translation key does not exist, false otherwise
     */
    private static boolean checkIfTranslationKeyExists(String translationKey, Project project) {
        return !FileBasedIndex.getInstance().processValues(
            TranslationIndex.INDEX_ID,
            translationKey,
            null,
            (file, value) -> false,
            GlobalSearchScope.allScope(project)
        );
    }

    /**
     * Checks if the translation key exists in the index
     *
     * @param translationKey The translation key to check
     * @param project        The current project
     * @return true if the translation key does not exist, false otherwise
     */
    private static boolean checkIfTranslationKeyExistsInServiceProvider(String translationKey, Project project) {
        AtomicBoolean found = new AtomicBoolean(false);
        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
        fileBasedIndex.processAllKeys(ServiceProviderIndex.INDEX_ID, key -> {
            fileBasedIndex.processValues(
                ServiceProviderIndex.INDEX_ID,
                key,
                null,
                (file, serviceProvider) -> {
                    for (Map.Entry<String, String> entry : serviceProvider.getTranslationKeys().entrySet()) {

                        if (entry.getKey().equals(translationKey)) {
                            found.set(true);
                            return false; // stop processing this value
                        }
                    }
                    return true; // continue processing other serviceProviders for this key
                },
                GlobalSearchScope.allScope(project)
            );

            return !found.get(); // stop iterating keys if found
        }, project);

        return found.get();
    }
}

package at.alirezamoh.whisperer_for_laravel.translation.resolver;

import at.alirezamoh.whisperer_for_laravel.indexes.TranslationIndex;
import at.alirezamoh.whisperer_for_laravel.support.utils.EnvUtils;
import com.intellij.json.JsonUtil;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.PhpReturnImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

/**
 * Resolves references to translation keys within Laravel translation files.
 * Assumes translations are defined as PHP arrays inside files located in:
 *  resources/lang/{locale}/<file>.php
 *  or lang/{locale}/<file>.php
 */
public class TranslationKeyResolver {
    private static final String PHP_EXTENSION = ".php";

    private static final String PATH_SEPARATOR = "/";

    private static final String DOT_SEPARATOR = ".";

    private static final String TRANSLATION_STORAGE_PATH_IN_RESOURCES_DIR = "/resources/lang/";

    private static final String TRANSLATION_STORAGE_PATH_OUTSIDE_RESOURCES_DIR = "/lang/";

    private static final TranslationKeyResolver INSTANCE = new TranslationKeyResolver();

    private TranslationKeyResolver() {}

    public static TranslationKeyResolver getInstance() {
        return INSTANCE;
    }

    /**
     * Resolves all occurrences of a translation key (e.g. "dashboard.user.profile")
     * in Laravel translation files across all languages
     *
     * @param text       The full translation key
     * @param project    The current project
     * @param psiManager The PsiManager to locate PsiFiles
     * @return A map of PsiElement instances to their containing PsiFiles
     */
    public @NotNull HashMap<PsiElement, PsiFile> resolveAllInTranslationFiles(@NotNull String text, @NotNull Project project, @NotNull PsiManager psiManager) {

        HashMap<PsiElement, PsiFile> results = new HashMap<>();
        Collection<VirtualFile> translationFiles = findTranslationFiles(text, project);

        for (VirtualFile file : translationFiles) {
            PsiFile psiFile = psiManager.findFile(file);
            if (psiFile == null) {
                continue;
            }

            if (psiFile instanceof JsonFile jsonFile) {
                processJsonFile(jsonFile, text, results);
                continue;
            }

            String relativePath = getRelativeTranslationFilePath(file);
            if (relativePath != null) {
                processPhpFile(psiFile, text, relativePath, results);
            }
        }

        return results;
    }

    /**
     * Finds all translation files that might contain the given translation key
     *
     * @param text    The translation key to search for
     * @param project The current project
     * @return A collection of virtual files that might contain the translation key
     */
    private Collection<VirtualFile> findTranslationFiles(String text, Project project) {
        String appLocale = EnvUtils.getAppLocale(project);

        if (appLocale == null) {
            return Collections.emptyList();
        }

        return FileBasedIndex.getInstance().getContainingFiles(
            TranslationIndex.INDEX_ID,
            appLocale + "|" + text,
            GlobalSearchScope.allScope(project)
        );
    }

    /**
     * Go through a JSON translation file to find translation keys
     *
     * @param jsonFile The JSON file to process
     * @param text     The translation key to find
     * @param results  Map to store the results in
     */
    private void processJsonFile(JsonFile jsonFile, String text, HashMap<PsiElement, PsiFile> results) {
        JsonObject jsonObject = JsonUtil.getTopLevelObject(jsonFile);
        if (jsonObject == null) {
            return;
        }

        if (text.contains(DOT_SEPARATOR)) {
            processNestedJsonKeys(jsonObject, text.split("\\."), results, jsonFile);
        } else {
            JsonProperty jsonProperty = jsonObject.findProperty(text);
            if (jsonProperty != null) {
                results.put(jsonProperty, jsonFile);
            }
        }
    }

    /**
     * Find the translation key in a nested JSON object (Laravel by default does snot support nested JSON objects)
     *
     * @param jsonObject The JSON object to search in
     * @param keys       The array of key parts
     * @param results    Map to store the results in
     * @param jsonFile   The containing JSON file
     */
    private void processNestedJsonKeys(JsonObject jsonObject, String[] keys,
                                       HashMap<PsiElement, PsiFile> results, JsonFile jsonFile) {
        JsonObject currentObject = jsonObject;
        JsonProperty finalProperty = null;

        for (String key : keys) {
            JsonProperty jsonProperty = currentObject.findProperty(key);
            if (jsonProperty == null) {
                break;
            }

            if (jsonProperty.getValue() instanceof JsonObject nestedObject) {
                currentObject = nestedObject;
            } else {
                finalProperty = jsonProperty;
                break;
            }
        }

        if (finalProperty != null) {
            results.put(finalProperty, jsonFile);
        }
    }

    /**
     * Go through a PHP translation file to find a translation key
     *
     * @param psiFile      The PHP file to process.
     * @param text         The translation key to find.
     * @param relativePath The relative path of the file.
     * @param results      Map to store the results in.
     */
    private void processPhpFile(PsiFile psiFile, String text, String relativePath, HashMap<PsiElement, PsiFile> results) {
        extractKeyPath(text, relativePath).ifPresent(keyPath -> {
            PsiElement element = findTranslationKeyInFile(psiFile, keyPath);
            if (element != null) {
                results.put(element, psiFile);
            }
        });
    }

    /**
     * Extracts the nested key portion from the full translation key,
     * For example, if fullKey is "dashboard.user.profile" and relativePath is "dashboard",
     * it returns "user.profile"
     *
     * @param fullKey      The full translation key
     * @param relativePath The relative path part of the key
     * @return An Optional containing the extracted key path or empty if not found
     */
    private @NotNull Optional<String> extractKeyPath(@NotNull String fullKey, @NotNull String relativePath) {
        String prefixWithDot = relativePath + DOT_SEPARATOR;
        if (fullKey.startsWith(prefixWithDot)) {
            return Optional.of(fullKey.substring(prefixWithDot.length()));
        } else if (fullKey.equals(relativePath)) {
            return Optional.of("");
        }
        return Optional.empty();
    }

    /**
     * Searches within a translation file (PHP file) for the nested translation key
     *
     * @param psiFile The PHP file to search in
     * @param key     The key to search for
     * @return The found PsiElement or null if not found
     */
    private @Nullable PsiElement findTranslationKeyInFile(@NotNull PsiFile psiFile, @NotNull String key) {
        if (key.isEmpty()) {
            return null;
        }

        PhpReturnImpl returnStatement = PsiTreeUtil.findChildOfType(psiFile, PhpReturnImpl.class);
        if (returnStatement == null) {
            return null;
        }

        ArrayCreationExpression arrayCreation = PsiTreeUtil.findChildOfType(returnStatement, ArrayCreationExpression.class);
        if (arrayCreation == null) {
            return null;
        }

        return navigateNestedArrays(arrayCreation, key);
    }

    /**
     * Navigates through nested arrays to find the specified key.
     *
     * @param startElement The starting array element.
     * @param keyPath      The key path to navigate.
     * @return The found PsiElement or null if not found.
     */
    private @Nullable PsiElement navigateNestedArrays(@NotNull PsiElement startElement, @NotNull String keyPath) {
        String[] keyParts = keyPath.split("\\.");
        PsiElement currentElement = startElement;

        for (int i = 0; i < keyParts.length; i++) {
            String currentKey = keyParts[i].trim();
            if (currentKey.isEmpty()) {
                return null;
            }

            ArrayHashElement hashElement = findArrayHashElement(currentElement, currentKey);
            if (hashElement == null) {
                return null;
            }

            if (i == keyParts.length - 1) {
                return hashElement.getValue();
            } else {
                currentElement = hashElement.getValue();
                if (!(currentElement instanceof ArrayCreationExpression)) {
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * Finds an array hash element with the specified key within a parent element.
     *
     * @param parent The parent element to search in.
     * @param key    The key to search for.
     * @return The found ArrayHashElement or null if not found.
     */
    private @Nullable ArrayHashElement findArrayHashElement(@NotNull PsiElement parent, @NotNull String key) {
        return PsiTreeUtil.findChildrenOfType(parent, ArrayHashElement.class)
            .stream()
            .filter(element -> {
                PhpPsiElement keyElement = element.getKey();
                return keyElement instanceof StringLiteralExpression &&
                    ((StringLiteralExpression) keyElement).getContents().trim().equals(key);
            })
            .findFirst()
            .orElse(null);
    }

    /**
     * Returns the relative translation file path (a pseudo-namespace) without the language folder.
     * For example, given:
     *   /project/resources/lang/en/dashboard/user.php OR /project/lang/en/dashboard/user.php
     * It returns "dashboard.user"
     *
     * @param translationFile The translation file.
     * @return The relative translation file path or null if invalid.
     */
    private @Nullable String getRelativeTranslationFilePath(@NotNull VirtualFile translationFile) {
        String filePath = translationFile.getPath().replace('\\', '/');
        int langIndex = filePath.indexOf(TRANSLATION_STORAGE_PATH_IN_RESOURCES_DIR);
        String pathToUse = TRANSLATION_STORAGE_PATH_IN_RESOURCES_DIR;
        if (langIndex == -1) {
            langIndex = filePath.indexOf(TRANSLATION_STORAGE_PATH_OUTSIDE_RESOURCES_DIR);
            pathToUse = TRANSLATION_STORAGE_PATH_OUTSIDE_RESOURCES_DIR;

            if (langIndex == -1) {
                return null;
            }
        }

        // Start after "/resources/lang/" or "/lang/"
        int start = langIndex + pathToUse.length();

        // Skip the locale folder (e.g., "en/"), then capture the remaining path
        int slashAfterLocale = filePath.indexOf(PATH_SEPARATOR, start);
        if (slashAfterLocale == -1) {
            return null;
        }

        String relativePath = filePath.substring(slashAfterLocale + 1);
        if (relativePath.endsWith(PHP_EXTENSION)) {
            relativePath = relativePath.substring(0, relativePath.length() - PHP_EXTENSION.length());
        }

        return relativePath.replace(PATH_SEPARATOR.charAt(0), DOT_SEPARATOR.charAt(0));
    }
}


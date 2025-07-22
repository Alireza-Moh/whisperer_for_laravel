package at.alirezamoh.whisperer_for_laravel.translation.util;

import at.alirezamoh.whisperer_for_laravel.config.visitors.ArrayReturnPsiRecursiveVisitor;
import at.alirezamoh.whisperer_for_laravel.indexes.TranslationIndex;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.WhispererForLaravelIcon;
import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import at.alirezamoh.whisperer_for_laravel.translation.PresentableTranslationElement;
import at.alirezamoh.whisperer_for_laravel.translation.resolver.TranslationKeyResolver;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.blade.psi.BladePsiLanguageInjectionHost;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

final public class TranslationUtil {
    private TranslationUtil() {}

    /**
     * The namespace of the `Lang` facade
     */
    private final static String LANG_FACADE_FQN_CLASS = "\\Illuminate\\Support\\Facades\\Lang";

    /**
     * The names of the methods in the `Lang` facade
     */
    public static Map<String, Integer> LANG_FACADE_METHODS = new HashMap<>() {{
        put("get", 0);
        put("has", 0);
        put("choice", 0);
    }};

    /**
     * The names of the translation helper functions
     */
    private static final Map<String, Integer> TRANSLATION_HELPER_FUNCTIONS = new HashMap<>() {{
        put("__", 0);
        put("trans_choice", 0);
    }};

    /**
     * Checks if the given PSI element is inside a translation helper function call
     * @param psiElement The PSI element to check
     * @return           True or false
     */
    public static boolean isInsideCorrectMethod(@NotNull PsiElement psiElement, Project project) {
        MethodReference method = MethodUtils.resolveMethodReference(psiElement, 4);
        FunctionReferenceImpl function = MethodUtils.resolveFunctionReference(psiElement, 4);

        return (
            method != null
                && isTranslationParam(method, psiElement)
                && PhpClassUtils.isCorrectRelatedClass(method, project, LANG_FACADE_FQN_CLASS)
        )
            || (
                function != null
                    && isTranslationParam(function, psiElement)
                    && TRANSLATION_HELPER_FUNCTIONS.containsKey(function.getName())
        );
    }

    /**
     * Collects translation keys from the given PsiFile
     *
     * @param project the current project
     * @param fileBasedIndex php file index
     * @param variants the list to store collected keys
     */
    public static void getKeysFromTranslationIndex(@NotNull Project project, FileBasedIndex fileBasedIndex, List<LookupElementBuilder> variants) {
        fileBasedIndex.processAllKeys(TranslationIndex.INDEX_ID, key -> {
            fileBasedIndex.processValues(TranslationIndex.INDEX_ID, key, null, (file, value) -> {
                    variants.add(buildLookupElement(key, buildKeyValue(value)));
                    return true;
                },
                GlobalSearchScope.allScope(project)
            );
            return true;
        }, project);
    }

    public static boolean isInsideBladeLangDirective(@NotNull PsiElement psiElement, Project project) {
        if (InjectedLanguageManager.getInstance(project).isInjectedFragment(psiElement.getContainingFile())) {
            PsiLanguageInjectionHost host = InjectedLanguageManager.getInstance(project).getInjectionHost(psiElement);
            if (host instanceof BladePsiLanguageInjectionHost) {
                PsiElement nthParent = PsiElementUtils.getNthParent(psiElement, 3);

                return nthParent != null && isLangDirective(nthParent);
            }
        }

        return false;
    }

    /**
     * Creates an array of ResolveResult objects from resolved translation keys
     *
     * @param translationKey The original translation key that was searched for
     * @param resolvedKeys   Map of resolved PsiElements and their containing files
     * @return An array of ResolveResult objects
     */
    public static ResolveResult @NotNull [] createResolveResults(@NotNull String translationKey, @NotNull HashMap<PsiElement, PsiFile> resolvedKeys, Project project) {

        List<ResolveResult> results = new ArrayList<>();
        for (Map.Entry<PsiElement, PsiFile> entry : resolvedKeys.entrySet()) {
            PsiElement originalElement = entry.getKey();
            PsiFile originalFile = entry.getValue();

            String filePath = originalFile.getVirtualFile().getPath()
                .replaceFirst("^" + project.getBasePath() + "/+", "");

            results.add(new PsiElementResolveResult(
                new PresentableTranslationElement(originalElement, filePath, translationKey)
            ));
        }

        return results.toArray(new ResolveResult[0]);
    }

    public static HashMap<PsiElement, PsiFile> resolveTranslationKey(Project project, String translationKey) {
        PsiManager psiManager = PsiManager.getInstance(project);
        TranslationKeyResolver translationKeyResolver = TranslationKeyResolver.getInstance();

        return translationKeyResolver.resolveAllInTranslationFiles(translationKey, project, psiManager);
    }

    public static String buildParentPathForTranslationKey(VirtualFile file, Project project, boolean forModule, String configKeyIdentifier) {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return "";
        }

        SettingsState settingsState = SettingsState.getInstance(project);
        if (!settingsState.isProjectDirectoryEmpty()) {
            basePath = basePath + "/" + StrUtils.addSlashes(settingsState.getProjectDirectoryPath(), true, true);
        }

        String fullPath = file.getPath();

        fullPath = fullPath.replaceFirst("(?i)^" + Pattern.quote(basePath), "");

        while (fullPath.startsWith("/")) {
            fullPath = fullPath.substring(1);
        }

        // Expect path like: resources/lang/en/messages.php
        // or resources/lang/en.json or lang/en/messages.php or lang/en.json
        // Strip up to and including "/lang/"
        int langIndex = fullPath.indexOf("lang/");
        if (langIndex != -1) {
            fullPath = fullPath.substring(langIndex + "lang/".length());
        } else {
            // Also check for "resources/lang/" directly
            int resourcesLangIndex = fullPath.indexOf("resources/lang/");
            if (resourcesLangIndex != -1) {
                fullPath = fullPath.substring(resourcesLangIndex + "resources/lang/".length());
            }
        }

        String[] parts = fullPath.split("/");

        if (parts.length < 2) {
            return "";
        }

        if (fullPath.endsWith(".json")) {
            return "";
        }

        // Remove the language directory (like "en/") part
        fullPath = fullPath.substring(fullPath.indexOf("/") + 1); // remove lang prefix

        // Remove .php extension
        if (fullPath.endsWith(".php")) {
            fullPath = fullPath.substring(0, fullPath.length() - 4);
        }

        fullPath = fullPath.replace('/', '.');

        if (forModule) {
            fullPath = configKeyIdentifier;
        }

        return fullPath;
    }

    public static void iterateOverFileChildren(String dirName, PsiFile configFile, Map<String, String> variants) {
        ArrayReturnPsiRecursiveVisitor visitor = new ArrayReturnPsiRecursiveVisitor(dirName);
        configFile.acceptChildren(visitor);

        variants.putAll(visitor.getVariants());
    }

    private static boolean isLangDirective(PsiElement psiElement) {
        String text = psiElement.getText();

        return text.startsWith("app('translator')->get")
            || text.startsWith("$__env->startTranslation()")
            || text.startsWith("\\$__env->startTranslation");
    }

    /**
     * Check if the given reference and position match the translation name parameter criteria
     * @param reference The function reference
     * @param position The PSI element position
     * @return True or false
     */
    private static boolean isTranslationParam(PsiElement reference, PsiElement position) {
        String referenceName = (reference instanceof MethodReference)
            ? ((MethodReference) reference).getName()
            : ((FunctionReferenceImpl) reference).getName();

        if (referenceName == null) {
            return false;
        }

        Integer expectedParamIndex = TRANSLATION_HELPER_FUNCTIONS.get(referenceName);
        if (expectedParamIndex == null) {
            expectedParamIndex = LANG_FACADE_METHODS.get(referenceName);
            if (expectedParamIndex == null) {
                return false;
            }
        }

        return MethodUtils.findParamIndex(position, false) == expectedParamIndex;
    }

    /**
     * Builds a key-value string for display purposes
     *
     * @param value The value to format
     * @return The formatted key-value string
     */
    private static @NotNull String buildKeyValue(String value) {
        return (value == null || value.isEmpty()) ? "" : " = " + value.trim();
    }

    /**
     * Builds a LookupElement for the translation key
     *
     * @param key   The translation key
     * @param value The value associated with the key
     * @return A LookupElementBuilder for the translation key
     */
    private static LookupElementBuilder buildLookupElement(String key, String value) {
        return LookupElementBuilder
            .create(key)
            .withLookupString(key)
            .withPresentableText(key)
            .withTailText(value, true)
            .bold()
            .withIcon(WhispererForLaravelIcon.LARAVEL_ICON);
    }
}

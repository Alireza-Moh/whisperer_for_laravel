package at.alirezamoh.whisperer_for_laravel.translation.util;

import at.alirezamoh.whisperer_for_laravel.indexes.TranslationIndex;
import at.alirezamoh.whisperer_for_laravel.support.WhispererForLaravelIcon;
import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import at.alirezamoh.whisperer_for_laravel.translation.PresentableTranslationElement;
import at.alirezamoh.whisperer_for_laravel.translation.resolver.TranslationKeyResolver;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.blade.psi.BladePsiLanguageInjectionHost;
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationUtil {
    /**
     * The names of the translation helper functions
     */
    private static final Map<String, Integer> TRANSLATION_METHODS = new HashMap<>() {{
        put("__", 0);
        put("trans_choice", 0);
    }};

    /**
     * Checks if the given PSI element is inside a translation helper function call
     * @param psiElement The PSI element to check
     * @return           True or false
     */
    public static boolean isInsideCorrectMethod(@NotNull PsiElement psiElement) {
        FunctionReferenceImpl function = MethodUtils.resolveFunctionReference(psiElement, 10);

        return function != null && isTranslationParam(function, psiElement);
    }

    /**
     * Processes all translation keys in the project index.
     */
    public static List<LookupElementBuilder> getTranslationKeysFromIndex(Project project) {
        List<LookupElementBuilder> variants = new ArrayList<>();
        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();

        fileBasedIndex.processAllKeys(TranslationIndex.INDEX_ID, key -> {
            fileBasedIndex.processValues(TranslationIndex.INDEX_ID, key, null, (file, value) -> {
                variants.add(buildLookupElement(key, buildKeyValue(value)));
                return true;
            }, GlobalSearchScope.allScope(project));
            return true;
        }, project);

        return variants;
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

    public static HashMap<PsiElement, PsiFile> getTranslationKeysFromIndex(Project project, String translationKey) {
        PsiManager psiManager = PsiManager.getInstance(project);
        TranslationKeyResolver translationKeyResolver = TranslationKeyResolver.getInstance();

        return translationKeyResolver.resolveAllInTranslationFiles(translationKey, project, psiManager);
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
    private static boolean isTranslationParam(FunctionReferenceImpl reference, PsiElement position) {
        String referenceName = reference.getName();

        if (referenceName == null) {
            return false;
        }

        Integer expectedParamIndex = TRANSLATION_METHODS.get(referenceName);
        if (expectedParamIndex == null) {
            return false;
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

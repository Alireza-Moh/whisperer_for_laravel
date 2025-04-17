package at.alirezamoh.whisperer_for_laravel.translation;

import at.alirezamoh.whisperer_for_laravel.indexes.TranslationIndex;
import at.alirezamoh.whisperer_for_laravel.support.WhispererForLaravelIcon;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import at.alirezamoh.whisperer_for_laravel.translation.resolver.TranslationKeyResolver;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.IdFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private Project project;

    public TranslationReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);

        this.project = element.getProject();
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean b) {
        String translationKey = StrUtils.removeQuotes(myElement.getText());
        PsiManager psiManager = PsiManager.getInstance(project);
        TranslationKeyResolver translationKeyResolver = TranslationKeyResolver.getInstance();

        HashMap<PsiElement, PsiFile> resolvedTranslationKeys =
            translationKeyResolver.resolveAllInTranslationFiles(translationKey, project, psiManager);

        if (resolvedTranslationKeys.isEmpty()) {
            return ResolveResult.EMPTY_ARRAY;
        }

        return createResolveResults(translationKey, resolvedTranslationKeys);
    }

    @Override
    public @Nullable PsiElement resolve() {
        return null;
    }

    /**
     * Provides autocompletion variants for translation keys.
     *
     * @return An array of LookupElementBuilder objects representing translation keys
     */
    @Override
    public Object @NotNull [] getVariants() {
        List<LookupElementBuilder> variants = new ArrayList<>();
        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        IdFilter filter = IdFilter.getProjectIdFilter(project, false);

        processAllTranslationKeys(fileBasedIndex, variants, scope, filter);

        return variants.toArray();
    }

    /**
     * Processes all translation keys in the project index.
     *
     * @param fileBasedIndex The file-based index to query
     * @param variants       List to store the created lookup elements
     * @param scope          The search scope
     * @param filter         The ID filter
     */
    private void processAllTranslationKeys(@NotNull FileBasedIndex fileBasedIndex, @NotNull List<LookupElementBuilder> variants, @NotNull GlobalSearchScope scope, @Nullable IdFilter filter) {
        fileBasedIndex.processAllKeys(
            TranslationIndex.INDEX_ID,
            key -> {
                processTranslationKey(fileBasedIndex, key, variants, scope, filter);
                return true;
            },
            scope,
            filter
        );
    }

    /**
     * Processes a single translation key and its values.
     *
     * @param fileBasedIndex The file-based index to query
     * @param key            The translation key
     * @param variants       List to store the created lookup elements
     * @param scope          The search scope
     * @param filter         The ID filter
     */
    private void processTranslationKey(
        @NotNull FileBasedIndex fileBasedIndex,
        @NotNull String key,
        @NotNull List<LookupElementBuilder> variants,
        @NotNull GlobalSearchScope scope,
        @Nullable IdFilter filter) {

        fileBasedIndex.processValues(
            TranslationIndex.INDEX_ID,
            key,
            null,
            (file, value) -> {
                variants.add(buildLookupElement(key, buildKeyValue(value)));
                return true;
            },
            scope,
            filter
        );
    }


    private @NotNull String buildKeyValue(String value) {
        return (value == null || value.isEmpty()) ? "" : " = " + value.trim();
    }

    private LookupElementBuilder buildLookupElement(String key, String value) {
        return LookupElementBuilder
            .create(key)
            .withLookupString(key)
            .withPresentableText(key)
            .withTailText(value, true)
            .bold()
            .withIcon(WhispererForLaravelIcon.LARAVEL_ICON);
    }

    /**
     * Creates an array of ResolveResult objects from resolved translation keys
     *
     * @param translationKey The original translation key that was searched for
     * @param resolvedKeys   Map of resolved PsiElements and their containing files
     * @return An array of ResolveResult objects
     */
    private ResolveResult @NotNull [] createResolveResults(@NotNull String translationKey, @NotNull HashMap<PsiElement, PsiFile> resolvedKeys) {

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

}

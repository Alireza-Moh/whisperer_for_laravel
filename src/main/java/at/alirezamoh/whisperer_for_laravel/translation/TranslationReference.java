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
        return getTranslationKeysFromIndex().toArray();
    }

    /**
     * Processes all translation keys in the project index.
     */
    private List<LookupElementBuilder> getTranslationKeysFromIndex() {
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

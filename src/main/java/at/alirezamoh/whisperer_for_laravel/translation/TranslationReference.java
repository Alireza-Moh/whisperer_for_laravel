package at.alirezamoh.whisperer_for_laravel.translation;

import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import at.alirezamoh.whisperer_for_laravel.translation.util.TranslationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class TranslationReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private Project project;

    public TranslationReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);

        this.project = element.getProject();
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean b) {
        String translationKey = StrUtils.removeQuotes(myElement.getText());
        HashMap<PsiElement, PsiFile> resolvedTranslationKeys = TranslationUtil.getTranslationKeysFromIndex(project, translationKey);

        if (resolvedTranslationKeys.isEmpty()) {
            return ResolveResult.EMPTY_ARRAY;
        }

        return TranslationUtil.createResolveResults(translationKey, resolvedTranslationKeys, project);
    }

    @Override
    public @Nullable PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @Override
    public Object @NotNull [] getVariants() {
        return TranslationUtil.getTranslationKeysFromIndex(project).toArray();
    }
}

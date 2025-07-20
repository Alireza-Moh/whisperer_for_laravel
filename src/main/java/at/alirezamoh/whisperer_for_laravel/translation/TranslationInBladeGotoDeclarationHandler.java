package at.alirezamoh.whisperer_for_laravel.translation;

import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import at.alirezamoh.whisperer_for_laravel.translation.util.TranslationUtil;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Navigates to the declaration of a translation key in blade templates.
 */
public class TranslationInBladeGotoDeclarationHandler implements GotoDeclarationHandler {

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor) {
        if (sourceElement == null) {
            return null;
        }

        Project project = sourceElement.getProject();

        if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
            return null;
        }

        if (TranslationUtil.isInsideBladeLangDirective(sourceElement, project)) {
            String translationKey = StrUtils.removeQuotes(sourceElement.getText());
            HashMap<PsiElement, PsiFile> resolvedTranslationKeys = TranslationUtil.getTranslationKeysFromIndex(project, translationKey);

            if (resolvedTranslationKeys.isEmpty()) {
                return null;
            }

            ResolveResult[] results = TranslationUtil.createResolveResults(translationKey, resolvedTranslationKeys, project);
            return Arrays.stream(results)
                .map(ResolveResult::getElement)
                .toArray(PsiElement[]::new);
        }

        return null;
    }

    @Override
    public @Nullable String getActionText(@NotNull DataContext context) {
        return null;
    }
}
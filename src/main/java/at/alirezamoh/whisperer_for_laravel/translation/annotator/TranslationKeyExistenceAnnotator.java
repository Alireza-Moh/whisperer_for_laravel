package at.alirezamoh.whisperer_for_laravel.translation.annotator;

import at.alirezamoh.whisperer_for_laravel.indexes.TranslationIndex;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectLocaleLangResolver;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import at.alirezamoh.whisperer_for_laravel.translation.util.TranslationUtil;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

/**
 * Annotator that checks if a translation key exists.
 */
public class TranslationKeyExistenceAnnotator implements Annotator {
    /**
     * Inspects the given PSI element and adds a warning annotation if the translation key does not exist
     *
     * @param psiElement       the PSI element to inspect
     * @param annotationHolder the holder to register annotations
     */
    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        Project project = psiElement.getProject();
        if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
            return;
        }

        SettingsState settingsState = SettingsState.getInstance(project);
        if (!settingsState.isRouteNotFoundAnnotatorWarning()) {
            return;
        }

        if (!(psiElement instanceof StringLiteralExpression stringLiteralExpression)) {
            return;
        }

        if (!TranslationUtil.isInsideCorrectMethod(stringLiteralExpression)) {
            return;
        }

        if (!doesTranslationKeyExists(stringLiteralExpression, project)) {
            annotationHolder.newAnnotation(HighlightSeverity.WARNING, "Translation key not found")
                .range(stringLiteralExpression.getTextRange())
                .create();
        }
    }

    /**
     * Check if the translation key exists.
     *
     * @param stringLiteralExpression The psi element for the translation key
     * @param project   The current project
     * @return true or false
     */
    public static boolean doesTranslationKeyExists(StringLiteralExpression stringLiteralExpression, Project project) {
        String cleanedTranslationKey = StrUtils.removeQuotes(stringLiteralExpression.getText());
        String appLocale = ProjectLocaleLangResolver.loadProjectLocale(project);

        String translationKey = cleanedTranslationKey;
        if (appLocale != null) {
            translationKey = appLocale + "|" + cleanedTranslationKey;
        }

        return checkIfTranslationKeyExists(translationKey, project);
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
}

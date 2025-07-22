package at.alirezamoh.whisperer_for_laravel.translation.annotator;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.translation.util.TranslationUtil;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
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
        if (!settingsState.isTranslationKeyNotFoundAnnotatorWarning()) {
            return;
        }

        if (TranslationUtil.isInsideBladeLangDirective(psiElement, project)) {
            return;
        }

        if (!(psiElement instanceof StringLiteralExpression stringLiteralExpression)) {
            return;
        }

        if (!TranslationUtil.isInsideCorrectMethod(psiElement, project)) {
            return;
        }

        if (TranslationAnnotatorUtil.doesTranslationKeyNotExists(stringLiteralExpression, project)) {
            annotationHolder.newAnnotation(HighlightSeverity.WARNING, "Translation key not found")
                .range(stringLiteralExpression.getTextRange())
                .create();
        }
    }
}

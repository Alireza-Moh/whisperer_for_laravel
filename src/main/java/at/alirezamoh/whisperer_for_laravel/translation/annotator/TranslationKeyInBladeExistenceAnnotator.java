package at.alirezamoh.whisperer_for_laravel.translation.annotator;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.blade.psi.BladePsiDirective;
import com.jetbrains.php.blade.psi.BladeTokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Annotator that checks if a translation key exists.
 */
public class TranslationKeyInBladeExistenceAnnotator implements Annotator {
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

        PsiElement parent = PsiElementUtils.getNthParent(psiElement, 2);
        if (psiElement.getNode().getElementType() != BladeTokenTypes.DIRECTIVE_PARAMETER_CONTENT || parent == null) {
            return;
        }

        if (!(parent instanceof BladePsiDirective bladePsiDirective)) {
            return;
        }

        String directiveName = bladePsiDirective.getName();
        if (directiveName == null || !directiveName.equals("@lang")) {
            return;
        }

        if (TranslationAnnotatorUtil.doesTranslationKeyNotExists(psiElement, project)) {
            annotationHolder.newAnnotation(HighlightSeverity.WARNING, "Translation key not found")
                .range(psiElement.getTextRange())
                .create();
        }
    }
}

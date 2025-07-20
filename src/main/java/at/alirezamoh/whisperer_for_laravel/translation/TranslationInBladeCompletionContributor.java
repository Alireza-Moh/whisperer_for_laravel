package at.alirezamoh.whisperer_for_laravel.translation;

import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.translation.util.TranslationUtil;
import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.*;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * Provides code completion for translation keys in Blade templates
 */
public class TranslationInBladeCompletionContributor extends CompletionContributor {
    TranslationInBladeCompletionContributor() {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(PsiElement.class),
            new CompletionProvider<>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                    PsiElement position = completionParameters.getPosition();
                    Project project = position.getProject();

                    if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
                        return;
                    }

                    if (TranslationUtil.isInsideBladeLangDirective(position, project)) {
                        completionResultSet.addAllElements(TranslationUtil.getTranslationKeysFromIndex(project));
                    }
                }
            }
        );
    }
}

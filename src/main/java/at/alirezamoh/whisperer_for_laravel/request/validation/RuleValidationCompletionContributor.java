package at.alirezamoh.whisperer_for_laravel.request.validation;

import at.alirezamoh.whisperer_for_laravel.request.validation.util.RuleValidationUtil;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import org.jetbrains.annotations.NotNull;


/**
 * Contributes completion suggestions for Laravel validation rules
 */
public class RuleValidationCompletionContributor extends CompletionContributor {
    RuleValidationCompletionContributor() {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL),
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE)
            ),
            new CompletionProvider<>() {

                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                    PsiElement psiElement = completionParameters.getPosition().getOriginalElement().getParent();
                    Project project = psiElement.getProject();

                    if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
                        return;
                    }

                    if (RuleValidationUtil.isInsideCorrectMethod(psiElement, project)) {
                        CompletionResultSet resultOnPipe = RuleValidationUtil.getCompletionResultSetOnPipe(psiElement, completionResultSet, completionParameters);
                        if (resultOnPipe != null) {
                            completionResultSet = resultOnPipe;
                        }

                        RuleValidationUtil.createLookUpElement(completionResultSet);
                    }
                }
            }
        );
    }
}

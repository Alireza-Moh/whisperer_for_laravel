package at.alirezamoh.whisperer_for_laravel.request.validation;

import at.alirezamoh.whisperer_for_laravel.request.validation.util.RuleValidationUtil;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.whisperer_for_laravel.support.psiUtil.PsiUtil;
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

                    if (FrameworkUtils.isLaravelFrameworkNotInstalled(project)) {
                        return;
                    }

                    if (RuleValidationUtil.isInsideCorrectMethod(psiElement, project)) {
                        CompletionResultSet resultOnPipe = getCompletionResultSetOnPipe(psiElement, completionResultSet, completionParameters);
                        if (resultOnPipe != null) {
                            completionResultSet = resultOnPipe;
                        }

                        createLookUpElement(completionResultSet);
                    }
                }
            });
    }

    /**
     * Creates completion validation rules and adds them to the result set
     *
     * @param result The completion result set
     */
    private void createLookUpElement(@NotNull CompletionResultSet result) {
        for (String key : RuleValidationUtil.RULES) {
            result.addElement(PsiUtil.buildSimpleLookupElement(key));
        }
    }

    /**
     * Returns a new CompletionResultSet with a prefix matcher based on the text after the last pipe symbol
     * This method checks if the current PSI element's text contains a pipe symbol (|).
     * If it does, it creates a new CompletionResultSet with a prefix matcher that matches
     * the text after the last pipe symbol. This allows for accurate completion suggestions
     * when the user is typing validation rules separated by pipes
     * @return The new CompletionResultSet with a prefix matcher, or null if no pipe symbol is found
     */
    private CompletionResultSet getCompletionResultSetOnPipe(PsiElement psiElement, CompletionResultSet currentCompletionResult, CompletionParameters completionParameters) {
        String text = psiElement.getText();
        CompletionResultSet newCompletionResult = null;

        if (text.contains("|")) {
            int pipeIndex = text.lastIndexOf('|', completionParameters.getOffset() - psiElement.getTextRange().getStartOffset() - 1);
            String newText = text.substring(pipeIndex + 1, completionParameters.getOffset() - psiElement.getTextRange().getStartOffset());

            newCompletionResult = currentCompletionResult.withPrefixMatcher(newText);
        }

        return newCompletionResult;
    }
}

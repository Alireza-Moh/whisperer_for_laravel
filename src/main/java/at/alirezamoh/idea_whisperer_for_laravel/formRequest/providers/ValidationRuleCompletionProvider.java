package at.alirezamoh.idea_whisperer_for_laravel.formRequest.providers;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;

/**
 * Provides completion suggestions for Laravel validation rules
 * This class handles the scenario where the user is typing a validation rule
 * after a pipe symbol (|) in a Form Request class
 */
public class ValidationRuleCompletionProvider {
    /**
     * The PSI element being processed
     */
    private PsiElement psiElement;

    /**
     * The current completion result set
     */
    private CompletionResultSet currentCompletionResult;

    /**
     * The completion parameters
     */
    private CompletionParameters parameters;

    /**
     * @param psiElement              The PSI element being processed
     * @param currentCompletionResult The current completion result set
     * @param parameters              The completion parameters
     */
    public ValidationRuleCompletionProvider(PsiElement psiElement, CompletionResultSet currentCompletionResult, CompletionParameters parameters) {
        this.psiElement = psiElement;
        this.currentCompletionResult = currentCompletionResult;
        this.parameters = parameters;
    }

    /**
     * Returns a new CompletionResultSet with a prefix matcher based on the text after the last pipe symbol
     * This method checks if the current PSI element's text contains a pipe symbol (|).
     * If it does, it creates a new CompletionResultSet with a prefix matcher that matches
     * the text after the last pipe symbol. This allows for accurate completion suggestions
     * when the user is typing validation rules separated by pipes
     * @return The new CompletionResultSet with a prefix matcher, or null if no pipe symbol is found
     */
    public CompletionResultSet getCompletionResultSetOnPipe() {
        String text = this.psiElement.getText();
        CompletionResultSet newCompletionResult = null;

        if (text.contains("|")) {
            int pipeIndex = text.lastIndexOf('|', this.parameters.getOffset() - this.psiElement.getTextRange().getStartOffset() - 1);
            String newText = text.substring(pipeIndex + 1, this.parameters.getOffset() - this.psiElement.getTextRange().getStartOffset());

            newCompletionResult = this.currentCompletionResult.withPrefixMatcher(newText);
        }

        return newCompletionResult;
    }
}

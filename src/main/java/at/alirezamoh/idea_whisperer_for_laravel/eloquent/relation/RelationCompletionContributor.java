package at.alirezamoh.idea_whisperer_for_laravel.eloquent.relation;


import at.alirezamoh.idea_whisperer_for_laravel.eloquent.relation.utils.RelationResolver;
import at.alirezamoh.idea_whisperer_for_laravel.eloquent.utls.EloquentUtil;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RelationCompletionContributor extends CompletionContributor {
    public RelationCompletionContributor() {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL),
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE)
            ),
            new CompletionProvider<>() {

                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                    PsiElement psiElement = completionParameters.getPosition().getParent();

                    boolean s = EloquentUtil.isInsideCorrectRelationMethodMethod(psiElement);
                    if (EloquentUtil.isInsideCorrectRelationMethodMethod(psiElement)) {

                        List<LookupElementBuilder> ss = RelationResolver.getVariants(psiElement, psiElement.getProject());
                        completionResultSet.addAllElements(RelationResolver.getVariants(psiElement, psiElement.getProject()));
                    }
                }
            }
        );
    }

    public CompletionResultSet getCompletionResultSetOnPoint(CompletionParameters completionParameters, PsiElement psiElement, CompletionResultSet completionResultSet) {
        PsiElement originalElement = psiElement.getOriginalElement();
        String text = originalElement.getText();
        CompletionResultSet newCompletionResult = completionResultSet;

        if (text.contains(".")) {
            int endOffset = completionParameters.getOffset() - originalElement.getTextRange().getStartOffset();
            int pipeIndex = text.lastIndexOf('.', endOffset - 1);

            if (pipeIndex != -1 && pipeIndex + 1 < text.length() && endOffset <= text.length() && pipeIndex + 1 <= endOffset) {
                String newText = text.substring(pipeIndex + 1, endOffset);
                newCompletionResult = completionResultSet.withPrefixMatcher(newText);
            }
        }

        completionResultSet.restartCompletionOnAnyPrefixChange();
        return newCompletionResult;
    }
}

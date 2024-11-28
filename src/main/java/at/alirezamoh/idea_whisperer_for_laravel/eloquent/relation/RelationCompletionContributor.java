package at.alirezamoh.idea_whisperer_for_laravel.eloquent.relation;


import at.alirezamoh.idea_whisperer_for_laravel.eloquent.relation.utils.RelationResolver;
import at.alirezamoh.idea_whisperer_for_laravel.eloquent.utls.EloquentUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.LaravelPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.MethodUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.MethodReference;
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
                    PsiElement psiElement = completionParameters.getPosition().getParent().getOriginalElement();

                    if (EloquentUtil.isInsideCorrectRelationMethodMethod(psiElement)) {
                        completionResultSet = getCompletionResultSetOnPoint(completionParameters, psiElement, completionResultSet);
                        RelationResolver relationResolver = new RelationResolver();

                        completionResultSet.addAllElements(
                            relationResolver.getVariants(psiElement, psiElement.getProject())
                        );
                    }

            /*        boolean s = EloquentUtil.isInsideCorrectRelationMethodMethod(psiElement);
                    if (EloquentUtil.isInsideCorrectRelationMethodMethod(psiElement)) {

                        completionResultSet = getCompletionResultSetOnPoint(completionParameters, psiElement, completionResultSet);

                        //completionResultSet.addAllElements(RelationResolver.getNestedVariants(psiElement, psiElement.getProject()));
                    }*/
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

        return newCompletionResult;
    }

/*    public @NotNull List<LookupElementBuilder> getVariants(PsiElement myElement) {
        Project project = myElement.getProject();
        MethodReference methodReference = MethodUtils.resolveMethodReference(myElement, 10);

        if (methodReference != null && EloquentUtil.isInsideModelQueryRelationClosure(methodReference, project)) {
            MethodReference parent = EloquentUtil.getParentOfClosure(methodReference);
            if (parent != null) {
                PsiElement param = parent.getParameter(
                    LaravelPaths.QUERY_RELATION_PARAMS.get(parent.getName())
                );

                if (!myElement.getText().isEmpty()) {
                    return RelationResolver.getVariantsForRelation(myElement, project);
                }
                else {
                    return RelationResolver.getVariantsForRelation(param, project);
                }
            }
        }

        return RelationResolver.getVariants(myElement, project);
    }*/
}

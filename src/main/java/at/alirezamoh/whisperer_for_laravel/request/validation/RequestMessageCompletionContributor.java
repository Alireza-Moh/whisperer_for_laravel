package at.alirezamoh.whisperer_for_laravel.request.validation;

import at.alirezamoh.whisperer_for_laravel.request.validation.util.ValidationFieldAndRuleExtractor;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl;
import org.jetbrains.annotations.NotNull;


/**
 * Contributes completion suggestions for Laravel form request messages
 */
public class RequestMessageCompletionContributor extends CompletionContributor {
    RequestMessageCompletionContributor() {
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

                    if (isInsideMessagesMethod(psiElement)) {
                        ValidationFieldAndRuleExtractor.provideMessageCompletion(psiElement, completionResultSet);
                    }
                }
            }
        );
    }

    /**
     * Checks if the PSI element is inside a MethodImpl with the name 'messages'
     */
    private boolean isInsideMessagesMethod(PsiElement psiElement) {
        MethodImpl methodCall = PsiTreeUtil.getParentOfType(psiElement, MethodImpl.class);

        return (
            methodCall != null
            && methodCall.getName().equals("messages")
            && isInsideArrayKey(psiElement)
        )
            || PsiElementUtils.isRegularArray(psiElement, 5);
    }

    private boolean isInsideArrayKey(PsiElement psiElement) {
        return PsiElementUtils.isAssocArray(psiElement, 10) && PsiElementUtils.isInArrayKey(psiElement, 10);
    }
}

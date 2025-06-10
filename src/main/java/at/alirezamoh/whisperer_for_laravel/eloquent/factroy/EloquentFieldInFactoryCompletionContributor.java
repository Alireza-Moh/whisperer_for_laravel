package at.alirezamoh.whisperer_for_laravel.eloquent.factroy;

import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.whisperer_for_laravel.support.utils.*;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class EloquentFieldInFactoryCompletionContributor extends CompletionContributor {
    EloquentFieldInFactoryCompletionContributor() {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL),
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE)
            ),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                    PsiElement psiElement = completionParameters.getPosition().getOriginalElement().getParent();
                    Project project = psiElement.getProject();


                    if (!PluginUtils.isLaravelProject(project) && PluginUtils.isLaravelFrameworkNotInstalled(project)) {
                        return;
                    }

                    if (isInsideMessagesMethod(psiElement)) {
                        List<Field> fields = EloquentModelFieldExtractorInFactory.extract(psiElement, project);

                        if (fields == null) {
                            return;
                        }

                        for (Field field : fields) {
                            LookupElementBuilder lookupElementBuilder = PsiElementUtils.buildSimpleLookupElement(field.getName());

                            completionResultSet.addElement(
                                PsiElementUtils.buildPrioritizedLookupElement(lookupElementBuilder, 1000)
                            );
                        }
                    }
                }
            }
        );
    }

    /**
     * Checks if the PSI element is inside a MethodImpl with the name 'definition'
     */
    private boolean isInsideMessagesMethod(PsiElement psiElement) {
        MethodImpl methodCall = PsiTreeUtil.getParentOfType(psiElement, MethodImpl.class);

        return methodCall != null
            && methodCall.getName().equals("definition")
            && PsiElementUtils.isInsideArrayKey(psiElement);
    }
}
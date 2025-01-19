package at.alirezamoh.whisperer_for_laravel.request.requestField;

import at.alirezamoh.whisperer_for_laravel.request.requestField.util.RequestFieldUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.ArrayIndexImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Provides code completion for fields defined in FormRequest rules
 */
public class RequestFieldCompletionContributor extends CompletionContributor {
    public RequestFieldCompletionContributor() {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(
                PlatformPatterns.psiElement().withElementType(PhpTokenTypes.IDENTIFIER),
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL)
                    .withSuperParent(2, ArrayIndexImpl.class)
                    .withSuperParent(3, ArrayAccessExpression.class),
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL)
                    .withSuperParent(2, ArrayIndexImpl.class)
                    .withSuperParent(3, ArrayAccessExpression.class),
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE)
                    .withSuperParent(2, ArrayIndexImpl.class)
                    .withSuperParent(3, ArrayAccessExpression.class),
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE)
                    .withSuperParent(2, ArrayIndexImpl.class)
                    .withSuperParent(3, ArrayAccessExpression.class)
            ),
            new CompletionProvider<>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                    PsiElement position = parameters.getPosition().getOriginalElement();

                    Project project = position.getProject();
                    if (!PluginUtils.isLaravelProject(project) && PluginUtils.isLaravelFrameworkNotInstalled(project)) {
                        return;
                    }

                    PsiElement targetElement = getTargetElement(position);
                    if (targetElement instanceof VariableImpl variable) {
                        handleVariableCompletions(variable, project, resultSet, position);
                    } else if (targetElement instanceof MethodReference methodRef) {
                        handleVariableCompletions(methodRef, project, resultSet, position);
                    }
                }
            }
        );
        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL),
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE)
            ),
            new CompletionProvider<>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                    PsiElement position = parameters.getPosition().getOriginalElement();

                    Project project = position.getProject();
                    if (!PluginUtils.isLaravelProject(project) && PluginUtils.isLaravelFrameworkNotInstalled(project)) {
                        return;
                    }

                    if (RequestFieldUtils.isInsideCorrectMethod(position, project)) {
                        MethodReference methodReference = MethodUtils.resolveMethodReference(position, 10);
                        handleVariableCompletions(methodReference, project, resultSet, position);
                    }
                }
            }
        );
    }

    private PsiElement getTargetElement(PsiElement position) {
        PsiElement parent = position.getParent().getParent().getParent();
        if (parent instanceof ArrayAccessExpression arrayAccess) {
            return arrayAccess.getValue();
        }

        PsiElement prevSibling = position.getPrevSibling();

        return prevSibling != null ? prevSibling.getPrevSibling() : null;
    }

    private void handleVariableCompletions(
        PsiElement element,
        Project project,
        CompletionResultSet resultSet,
        PsiElement contextElement
    ) {
        PhpClassImpl phpClass = RequestFieldUtils.resolvePhpClass(element, project);

        if (phpClass == null) {
            return;
        }

        Collection<ArrayHashElement> rules = RequestFieldUtils.resolveRulesFromVariable(phpClass, project, contextElement);
        RequestFieldUtils.processRules(rules, resultSet);
    }
}


package at.alirezamoh.whisperer_for_laravel.request.requestField;

import at.alirezamoh.whisperer_for_laravel.request.requestField.util.RequestFieldUtils;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.whisperer_for_laravel.support.psiUtil.PsiUtil;
import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl;
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
            PlatformPatterns.psiElement().withElementType(PhpTokenTypes.IDENTIFIER),
            new CompletionProvider<>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                    PsiElement element = parameters.getPosition().getOriginalElement();

                    Project project = element.getProject();
                    if (!FrameworkUtils.isLaravelProject(project) && FrameworkUtils.isLaravelFrameworkNotInstalled(project)) {
                        return;
                    }

                    if (element.getPrevSibling() == null) {
                        return;
                    }

                    PsiElement parent = element.getPrevSibling().getPrevSibling();

                    if (!(parent instanceof VariableImpl variable)) {
                        return;
                    }

                    PhpClassImpl phpClass = RequestFieldUtils.resolveRequestClass(variable, project);
                    if (phpClass == null) {
                        return;
                    }

                    Collection<ArrayHashElement> rules = RequestFieldUtils.getRules(phpClass, project);
                    if (rules == null && RequestFieldUtils.REQUEST.equals(phpClass.getFQN())) {
                        MethodImpl method = PsiTreeUtil.getParentOfType(element, MethodImpl.class);
                        if (method != null) {
                            RequestFieldUtils.extractValidationRulesFromMethod(method).forEach(rule -> addCompletionFromRule(rule, resultSet));
                        }
                    } else if (rules != null) {
                        rules.forEach(rule -> addCompletionFromRule(rule, resultSet));
                    }
                }
            }
        );
    }

    private void addCompletionFromRule(ArrayHashElement rule, CompletionResultSet resultSet) {
        PsiElement key = rule.getKey();
        if (key instanceof StringLiteralExpression stringLiteralExpression) {
            resultSet.addElement(
                PrioritizedLookupElement.withPriority(
                    PsiUtil.buildSimpleLookupElement(StrUtil.removeQuotes(stringLiteralExpression.getText())),
                    1000
                )
            );
        }
    }
}

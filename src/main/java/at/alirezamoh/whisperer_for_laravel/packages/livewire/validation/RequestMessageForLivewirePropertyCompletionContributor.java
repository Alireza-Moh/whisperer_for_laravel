package at.alirezamoh.whisperer_for_laravel.packages.livewire.validation;

import at.alirezamoh.whisperer_for_laravel.request.validation.util.RuleValidationUtil;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;


public class RequestMessageForLivewirePropertyCompletionContributor extends CompletionContributor {
    RequestMessageForLivewirePropertyCompletionContributor() {
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

                    if (LivewireValidationUtil.isInsideCorrectAttribute(psiElement, 1)) {
                        PhpAttribute phpAttribute = LivewireValidationUtil.getPhpAttribute(psiElement);

                        if (phpAttribute == null) {
                            return;
                        }

                        PsiElement parameter = phpAttribute.getParameter(0);
                        if (!(parameter instanceof ArrayCreationExpression arrayCreationExpression)) {
                            return;
                        }


                        PsiTreeUtil.findChildrenOfType(arrayCreationExpression, ArrayHashElement.class).forEach(rule -> {
                            PsiElement key = rule.getKey();

                            if (key instanceof StringLiteralExpression fieldName) {
                                String fieldNameOutQuoted = StrUtils.removeQuotes(fieldName.getText());

                                RuleValidationUtil.extractValidationRules(rule.getValue()).forEach(ruleString -> {
                                    completionResultSet.addElement(
                                        PsiElementUtils.buildSimpleLookupElement(fieldNameOutQuoted + "." + ruleString)
                                    );
                                });
                            }
                        });
                    }
                }
            }
        );
    }
}
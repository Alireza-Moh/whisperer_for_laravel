package at.alirezamoh.idea_whisperer_for_laravel.formRequest;

import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.MethodUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Contributes completion suggestions for Laravel validation rules
 */
public class RuleValidationCompletionContributor extends CompletionContributor {
    private String[] rulesKeysArray = {
        "accepted", "accepted_if:", "active_url", "after:", "after_or_equal:", "alpha",
        "alpha_dash", "alpha_num", "array", "ascii", "bail", "before:", "before_or_equal:",
        "between:", "boolean", "confirmed", "current_password", "date", "date_equal:",
        "date_format:", "decimal:", "declined", "declined_if:", "different:", "digits:",
        "digits_between:", "dimensions", "distinct", "doesnt_start_with:",
        "doesnt_end_with:", "email", "ends_with:", "enum", "exclude", "exclude_if:",
        "exclude_unless:", "exclude_with:", "exclude_without:", "exists:", "extensions:",
        "file", "filled", "gt:", "gte:", "image", "in", "in_array:", "integer", "ip",
        "ipv4", "ipv6", "json", "It:", "Ite:", "lowercase", "mac_address", "max:",
        "max_digits:", "mimetypes:", "mimes:", "min:", "min_digits:", "multiple_of:",
        "missing", "missing_if:", "missing_unless:", "missing_with:", "missing_with_all:",
        "not_in:", "not_regex:", "nullable", "numeric", "password", "present",
        "present_if:", "present_unless:", "present_with:", "present_with_all:",
        "prohibited", "prohibited_if:", "prohibited_unless:", "prohibits:", "regex:",
        "required", "required_if:", "required_if_accepted:", "required_unless:",
        "required_with:", "required_with_all:", "required_without:", "required_without_all:",
        "required_array_keys:", "same:", "size:", "starts_with:", "string", "timezone",
        "unique:", "uppercase", "url", "ulid", "uuid"
    };

    /**
     * List of the available methods for completion
     */
    public static Map<String, List<Integer>> RULES_METHODS = new HashMap<>() {{
        put("validate", List.of(0));
        put("make", List.of(1));
        put("validateWithBag", List.of(1));
    }};


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
                    PsiElement psiElement = completionParameters.getPosition().getParent();
                    Project project = psiElement.getProject();

                    if (FrameworkUtils.isLaravelFrameworkNotInstalled(project)) {
                        return;
                    }

                    if (psiElement instanceof StringLiteralExpression && isInsideCorrectMethod(psiElement, project)) {
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
     * Checks if the current context is valid for suggesting validation rules
     *
     * @param psiElement The current PSI element
     * @return True if the context is valid, false otherwise
     */
    private boolean isInsideCorrectMethod(PsiElement psiElement, Project project) {
        MethodReference methodReference = MethodUtils.resolveMethodReference(psiElement, 10);

        if (methodReference != null) {
            return isInsideRequestMethod(psiElement, methodReference, project)
                || isInsideValidatorMethod(psiElement, methodReference, project);
        }

        return isInsideRulesMethod(psiElement);
    }

    private boolean isInsideRequestMethod(PsiElement psiElement, MethodReference methodReference, Project project) {

        return ClassUtils.isLaravelRelatedClass(methodReference, project)
            && (
                Objects.equals(methodReference.getName(), "validate")
                || Objects.equals(methodReference.getName(), "validateWithBag")
            )
            && isRuleParam(methodReference, psiElement)
            && isInsideArrayOrArrayValue(psiElement);
    }

    private boolean isInsideValidatorMethod(PsiElement psiElement, MethodReference methodReference, Project project) {
        return ClassUtils.isLaravelRelatedClass(methodReference, project)
            && Objects.equals(methodReference.getName(), "make")
            && isRuleParam(methodReference, psiElement)
            && isInsideArrayOrArrayValue(psiElement);
    }

    /**
     * Checks if the PSI element is inside a MethodImpl with the name 'rules'
     */
    private boolean isInsideRulesMethod(PsiElement psiElement) {
        MethodImpl methodCall = PsiTreeUtil.getParentOfType(psiElement, MethodImpl.class);

        return methodCall != null && methodCall.getName().equals("rules") && isInsideArrayOrArrayValue(psiElement);
    }

    private boolean isInsideArrayOrArrayValue(PsiElement psiElement) {
        boolean isInsideArrayOrArrayValue = PsiUtil.isInRegularArray(psiElement, 10);

        if (PsiUtil.isAssocArray(psiElement)) {
            isInsideArrayOrArrayValue = PsiUtil.isInArrayValue(psiElement, 10);
        }
        return isInsideArrayOrArrayValue;
    }

    public boolean isRuleParam(MethodReference method, PsiElement position) {
        int paramIndex = MethodUtils.findParamIndex(position, false);
        List<Integer> paramPositions = RULES_METHODS.get(method.getName());

        return paramPositions != null && paramPositions.contains(paramIndex);
    }

    /**
     * Creates completion validation rules and adds them to the result set
     *
     * @param result The completion result set
     */
    private void createLookUpElement(@NotNull CompletionResultSet result) {
        for (String key : rulesKeysArray) {
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

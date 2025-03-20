package at.alirezamoh.whisperer_for_laravel.request.validation.util;

import at.alirezamoh.whisperer_for_laravel.request.requestField.util.RequestFieldUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RuleValidationUtil {
    public static String[] RULES = {
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
    public static Map<String, Integer> RULES_METHODS = new HashMap<>() {{
        put("validate", 0);
        put("make", 1);
        put("validateWithBag", 1);
    }};

    /**
     * Checks if the current context is valid for suggesting validation rules
     *
     * @param psiElement The current PSI element
     * @return True if the context is valid, false otherwise
     */
    public static boolean isInsideCorrectMethod(PsiElement psiElement, Project project) {
        MethodReference methodReference = MethodUtils.resolveMethodReference(psiElement, 10);

        if (methodReference != null) {
            return isInsideRequestMethod(psiElement, methodReference, project)
                || isInsideValidatorMethod(psiElement, methodReference, project)
                || isInsideLivewireValidateMethod(psiElement, methodReference, project);
        }

        return isInsideRulesMethod(psiElement);
    }

    public static boolean isInsideLivewireValidateMethod(PsiElement psiElement, MethodReference methodReference, Project project) {
        return PhpClassUtils.isCorrectRelatedClass(methodReference, project, "\\Livewire\\Component")
            && RequestFieldUtils.VALIDATION_METHODS.contains(methodReference.getName())
            && isRuleParam(methodReference, psiElement)
            && isInsideArrayValue(psiElement);
    }

    public static boolean isRuleParam(MethodReference method, PsiElement position) {
        Integer paramPositions = RULES_METHODS.get(method.getName());

        if (paramPositions == null) {
            return false;
        }

        return MethodUtils.findParamIndex(position, false) == paramPositions;
    }

    /**
     * Creates completion validation rules and adds them to the result set
     *
     * @param result The completion result set
     */
    public static void createLookUpElement(@NotNull CompletionResultSet result) {
        for (String key : RuleValidationUtil.RULES) {
            result.addElement(PsiElementUtils.buildSimpleLookupElement(key));
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
    public static CompletionResultSet getCompletionResultSetOnPipe(PsiElement psiElement, CompletionResultSet currentCompletionResult, CompletionParameters completionParameters) {
        String text = psiElement.getText();
        CompletionResultSet newCompletionResult = null;

        if (text.contains("|")) {
            int pipeIndex = text.lastIndexOf('|', completionParameters.getOffset() - psiElement.getTextRange().getStartOffset() - 1);
            String newText = text.substring(pipeIndex + 1, completionParameters.getOffset() - psiElement.getTextRange().getStartOffset());

            newCompletionResult = currentCompletionResult.withPrefixMatcher(newText);
        }

        return newCompletionResult;
    }

    private static boolean isInsideRequestMethod(PsiElement psiElement, MethodReference methodReference, Project project) {
        String methodName = methodReference.getName();

        return PhpClassUtils.isCorrectRelatedClass(methodReference, project, "\\Illuminate\\Http\\Request")
            && methodName != null
            && RequestFieldUtils.VALIDATION_METHODS.contains(methodName)
            && isRuleParam(methodReference, psiElement)
            && isInsideArrayValue(psiElement);
    }

    private static boolean isInsideValidatorMethod(PsiElement psiElement, MethodReference methodReference, Project project) {
        return PhpClassUtils.isCorrectRelatedClass(methodReference, project, "\\Illuminate\\Support\\Facades\\Validator")
            && Objects.equals(methodReference.getName(), "make")
            && isRuleParam(methodReference, psiElement)
            && isInsideArrayValue(psiElement);
    }

    /**
     * Checks if the PSI element is inside a MethodImpl with the name 'rules'
     */
    private static boolean isInsideRulesMethod(PsiElement psiElement) {
        MethodImpl methodCall = PsiTreeUtil.getParentOfType(psiElement, MethodImpl.class);

        return methodCall != null
            && methodCall.getName().equals("rules")
            && isInsideArrayValue(psiElement);
    }

    private static boolean isInsideArrayValue(PsiElement psiElement) {
        if (PsiElementUtils.isRegularArray(psiElement, 10)) {
            ArrayCreationExpression array = PsiElementUtils.getRegularArray(psiElement, 10);
            if (array != null) {
                return PsiElementUtils.isAssocArray(array.getParent(), 10) && PsiElementUtils.isInArrayValue(array, 10);
            }
            return false;
        }
        else {
            return PsiElementUtils.isAssocArray(psiElement, 10) && PsiElementUtils.isInArrayValue(psiElement, 10);
        }
    }
}

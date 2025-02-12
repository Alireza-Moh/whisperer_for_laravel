package at.alirezamoh.whisperer_for_laravel.request.validation.util;

import at.alirezamoh.whisperer_for_laravel.request.requestField.util.RequestFieldUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl;

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
                || isInsideValidatorMethod(psiElement, methodReference, project);
        }

        return isInsideRulesMethod(psiElement);
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

    public static boolean isRuleParam(MethodReference method, PsiElement position) {
        Integer paramPositions = RULES_METHODS.get(method.getName());

        if (paramPositions == null) {
            return false;
        }
        return MethodUtils.findParamIndex(position, false) == paramPositions;
    }
}

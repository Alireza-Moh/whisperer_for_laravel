package at.alirezamoh.whisperer_for_laravel.request.requestField.util;

import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.psiUtil.PsiUtil;
import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.FieldReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final public class RequestFieldUtils {
    /**
     * validation methods of the request class
     */
    public static final List<String> VALIDATION_METHODS = List.of("validateWithBag", "validate");

    /**
     * Request class fqn
     */
    public static String REQUEST = "\\Illuminate\\Http\\Request";

    /**
     * Base FormRequest class fqn
     */
    private static String BASE_FORM_REQUEST = "Illuminate\\Foundation\\Http\\FormRequest";

    /**
     * Resolves $this
     *
     * @param variable the variable to resolve
     * @param project the current project
     * @return the resolved PhpClassImpl instance or null if not found
     */
    public static PhpClassImpl resolveRequestClass(Variable variable, Project project) {
        PhpClassImpl phpClass = ClassUtils.getClassFromTypedElement(variable, project);

        if (phpClass == null) {
            PsiReference reference = variable.getReference();
            if (reference != null) {
                PsiElement resolved = reference.resolve();
                if (resolved instanceof PhpClass requestClass) {
                    phpClass = (PhpClassImpl) requestClass;
                }
            }
        }

        return phpClass;
    }

    /**
     * Retrieves the "rules" defined in a FormRequest class
     *
     * @param phpClass the FormRequest class
     * @param project the current project
     * @return a collection of rules or null if not found
     */
    public static Collection<ArrayHashElement> getRules(PhpClassImpl phpClass, Project project) {
        PhpClass baseFormRequest = ClassUtils.getClassByFQN(project, BASE_FORM_REQUEST);
        if (baseFormRequest != null && ClassUtils.isChildOf(phpClass, baseFormRequest)) {
            Method rulesMethod = phpClass.findMethodByName("rules");
            if (rulesMethod != null) {
                PhpReturn phpReturn = PsiTreeUtil.findChildOfType(rulesMethod, PhpReturn.class);
                if (phpReturn != null) {
                    ArrayCreationExpression array = PsiTreeUtil.findChildOfType(phpReturn, ArrayCreationExpression.class);
                    if (array != null) {
                        return PsiTreeUtil.findChildrenOfType(array, ArrayHashElement.class);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Extracts validation rules from a method if it references a validation method.
     *
     * @param method the method to analyze
     * @return a collection of ArrayHashElements representing the validation rules
     */
    public static Collection<ArrayHashElement> extractValidationRulesFromMethod(MethodImpl method) {
        if (method == null) {
            return List.of();
        }

        return PsiTreeUtil.findChildrenOfType(method, MethodReference.class).stream()
            .filter(methodReference -> {
                if (!VALIDATION_METHODS.contains(methodReference.getName())) {
                    return false;
                }

                PhpExpression reference = methodReference.getClassReference();
                if (!(reference instanceof VariableImpl variable)) {
                    return false;
                }

                PhpClassImpl phpClass = resolveRequestClass(variable, variable.getProject());
                return phpClass != null && phpClass.getFQN().equals(REQUEST);
            })
            .flatMap(methodReference -> {
                PsiElement parameter = methodReference.getParameter(1);
                if (parameter instanceof ArrayCreationExpression array) {
                    return PsiTreeUtil.findChildrenOfType(array, ArrayHashElement.class).stream();
                }
                return Stream.empty();
            })
            .collect(Collectors.toList());
    }

    /**
     * Checks whether a field reference matches a given rule key
     *
     * @param element the field rule to match
     * @param rule the rule to check
     * @return true or false
     */
    public static boolean isMatchingRule(@Nullable PsiElement element, ArrayHashElement rule) {
        PsiElement key = rule.getKey();
        if (!(key instanceof StringLiteralExpression ruleAsString)) {
            return false;
        }

        String ruleName = StrUtil.removeQuotes(ruleAsString.getText());

        if (element instanceof StringLiteralExpression stringLiteralExpression) {
            return Objects.equals(
                StrUtil.removeQuotes(stringLiteralExpression.getText()),
                ruleName
            );
        }

        if (element instanceof FieldReferenceImpl fieldReference && fieldReference.getName() != null) {
            return Objects.equals(
                StrUtil.removeQuotes(fieldReference.getName()),
                ruleName
            );
        }

        return false;
    }

    public static Collection<ArrayHashElement> resolveRulesFromVariable(VariableImpl variable, Project project, PsiElement contextElement) {
        PhpClassImpl phpClass = resolveRequestClass(variable, project);
        if (phpClass == null) {
            return null;
        }

        Collection<ArrayHashElement> rules = getRules(phpClass, project);
        if (rules == null && REQUEST.equals(phpClass.getFQN())) {
            MethodImpl method = PsiTreeUtil.getParentOfType(contextElement, MethodImpl.class);
            if (method != null) {
                rules = extractValidationRulesFromMethod(method);
            }
        }

        return rules;
    }

    public static void processRules(Collection<ArrayHashElement> rules, CompletionResultSet resultSet) {
        if (rules == null) return;

        rules.forEach(rule -> {
            PsiElement key = rule.getKey();
            if (key instanceof StringLiteralExpression stringLiteral) {
                resultSet.addElement(
                    PsiUtil.buildSimpleLookupElement(StrUtil.removeQuotes(stringLiteral.getText()))
                );
            }
        });
    }
}

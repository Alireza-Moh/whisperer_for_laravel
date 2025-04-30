package at.alirezamoh.whisperer_for_laravel.request.requestField.util;

import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.FieldReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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

    public static Map<String, Integer> REQUEST_METHODS = new HashMap<>() {{
        put("input", 0);
        put("string", 0);
        put("integer", 0);
        put("boolean", 0);
        put("float", 0);
    }};

    private final static String[] REQUEST_CLASSES = {
        "\\Illuminate\\Http\\Request",
        "\\Illuminate\\Support\\ValidatedInput"
    };

    /**
     * Resolves $this
     *
     * @param variable the variable to resolve
     * @param project the current project
     * @return the resolved PhpClassImpl instance or null if not found
     */
    public static PhpClassImpl resolveRequestClass(Variable variable, Project project) {
        PhpClassImpl phpClass = PhpClassUtils.getClassFromTypedElement(variable, project);

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
        PhpClass baseFormRequest = PhpClassUtils.getClassByFQN(project, BASE_FORM_REQUEST);
        if (baseFormRequest != null && PhpClassUtils.isChildOf(phpClass, baseFormRequest)) {
            Method rulesMethod = phpClass.findMethodByName("rules");
            if (rulesMethod != null) {
                return getRulesAsArray(rulesMethod);
            }
        }

        return null;
    }

    /**
     * Retrieves the "rules" defined in a FormRequest class
     *
     * @param rulesMethod the rules method
     * @return a collection of rules or null if not found
     */
    public static @Nullable Collection<ArrayHashElement> getRulesAsArray(Method rulesMethod) {
        PhpReturn phpReturn = PsiTreeUtil.findChildOfType(rulesMethod, PhpReturn.class);
        if (phpReturn != null) {
            ArrayCreationExpression array = PsiTreeUtil.findChildOfType(phpReturn, ArrayCreationExpression.class);
            if (array != null) {
                return PsiTreeUtil.findChildrenOfType(array, ArrayHashElement.class);
            }
        }
        return null;
    }

    /**
     * Analyzes the given method to extract validation rules defined through calls to known validation methods
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
                String methodName = methodReference.getName();
                if (methodName == null || !VALIDATION_METHODS.contains(methodName)) {
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

        String ruleName = StrUtils.removeQuotes(ruleAsString.getText());

        if (element instanceof StringLiteralExpression stringLiteralExpression) {
            return Objects.equals(
                StrUtils.removeQuotes(stringLiteralExpression.getText()),
                ruleName
            );
        }

        if (element instanceof FieldReferenceImpl fieldReference && fieldReference.getName() != null) {
            return Objects.equals(
                StrUtils.removeQuotes(fieldReference.getName()),
                ruleName
            );
        }

        return false;
    }

    /**
     * Get all the rules from the rules method
     *
     * @param phpClass the PHP class to resolve the rules from
     * @param project the current project
     * @param contextElement the current context element
     * @return a collection of ArrayHashElements representing the rules
     */
    public static Collection<ArrayHashElement> resolveRulesFromVariable(PhpClass phpClass, Project project, PsiElement contextElement) {
        if (!(phpClass instanceof PhpClassImpl phpClassImpl)) {
            return List.of();
        }

        Collection<ArrayHashElement> rules = getRules(phpClassImpl, project);
        if (rules == null && REQUEST.equals(phpClass.getFQN())) {
            MethodImpl method = PsiTreeUtil.getParentOfType(contextElement, MethodImpl.class);
            if (method != null) {
                rules = extractValidationRulesFromMethod(method);
            }
        }

        return rules;
    }

    /**
     * Process the rules and add them to the completion result set
     * @param rules the validation rules
     * @param resultSet the completion result set
     */
    public static void processRules(Collection<ArrayHashElement> rules, CompletionResultSet resultSet) {
        if (rules == null) return;

        rules.forEach(rule -> {
            PsiElement key = rule.getKey();
            if (key instanceof StringLiteralExpression stringLiteral) {
                resultSet.addElement(
                    PsiElementUtils.buildSimpleLookupElement(StrUtils.removeQuotes(stringLiteral.getText()))
                );
            }
        });
    }

    /**
     * Resolves the PHP class from a given element
     *
     * @param element the element to resolve
     * @param project the current project
     * @return the resolved PHP class or null if not found
     */
    public static @Nullable PhpClassImpl resolvePhpClass(PsiElement element, Project project) {
        if (element instanceof VariableImpl variable) {
            PhpClassImpl phpClass = resolveRequestClass(variable, project);

            if (phpClass == null) {
                Query<PsiReference> references = ReferencesSearch.search(variable.getOriginalElement(), GlobalSearchScope.projectScope(project), false);

                for (PsiReference reference : references) {
                    PsiElement parent = reference.getElement().getParent();
                    if (parent instanceof AssignmentExpression assignmentExpression) {
                        PsiElement expression = assignmentExpression.getValue();

                        if (expression instanceof NewExpression newExpression) {
                            return PhpClassUtils.getClassFromTypedElement(newExpression.getClassReference(), project);
                        } else if (expression instanceof MethodReference methodReference) {
                            return PhpClassUtils.getClassFromTypedElement(methodReference.getClassReference(), project);
                        }
                    }
                }
            }

            return phpClass;
        } else if (element instanceof MethodReference methodRef) {
            return PhpClassUtils.getClassFromTypedElement(methodRef.getClassReference(), project);
        }

        return null;
    }

    /**
     * Checks if the current context is valid for suggesting request fields
     *
     * @param psiElement The current PSI element
     * @return True if the context is valid, false otherwise
     */
    public static boolean isInsideCorrectMethod(PsiElement psiElement, Project project) {
        MethodReference methodReference = MethodUtils.resolveMethodReference(psiElement, 10);

        return methodReference != null
            && methodReference.getName() != null
            && PhpClassUtils.isCorrectRelatedClass(methodReference, project, REQUEST_CLASSES)
            && REQUEST_METHODS.containsKey(methodReference.getName())
            && isFieldParam(methodReference, psiElement);
    }

    /**
     * Gets the parent element at a specific depth
     *
     * @param element the target element
     * @return founded parent element or null
     */
    public static @Nullable PsiElement getNthParent(PsiElement element, int n) {
        PsiElement current = element;

        for (int i = 0; i < n; i++) {
            if (current == null) {
                return null;
            }
            current = current.getParent();
        }

        return current;
    }

    /**
     * Checks if the given method reference is a request method to get field
     *
     * @param method the method reference to check
     * @param position the current PSI element
     * @return true or false
     */
    private static boolean isFieldParam(MethodReference method, PsiElement position) {
        String methodName = method.getName();
        if (methodName == null) {
            return false;
        }

        Integer paramPositions = REQUEST_METHODS.get(methodName);
        if (paramPositions == null) {
            return false;
        }

        return MethodUtils.findParamIndex(position, false) == paramPositions;
    }
}

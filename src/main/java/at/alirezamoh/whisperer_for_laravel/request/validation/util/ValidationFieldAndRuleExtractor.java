package at.alirezamoh.whisperer_for_laravel.request.validation.util;

import at.alirezamoh.whisperer_for_laravel.request.requestField.util.RequestFieldUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Utility class to extract validation rules from FormRequest classes
 * and provide autocompletion support for error messages
 */
public class ValidationFieldAndRuleExtractor {
    /**
     * Extracts validation rules and adds completion suggestions for message overrides
     *
     * @param psiElement The PSI element in the `messages()` method
     * @param result     The completion result set to populate
     */
    public static void provideMessageCompletion(PsiElement psiElement, @NotNull CompletionResultSet result) {
        MethodImpl messagesMethod = PsiTreeUtil.getParentOfType(psiElement, MethodImpl.class);

        if (messagesMethod != null && messagesMethod.getName().equals("messages")) {
            MethodImpl rulesMethod = findRulesMethod(messagesMethod);
            if (rulesMethod == null) {
                return;
            }

            Collection<ArrayHashElement> rules = RequestFieldUtils.getRulesAsArray(rulesMethod);
            if (rules == null) {
                return;
            }

            rules.forEach(rule -> {
                PsiElement key = rule.getKey();

                if (key instanceof StringLiteralExpression fieldName) {
                    String fieldNameOutQuoted = StrUtils.removeQuotes(fieldName.getText());
                    extractValidationRules(rule.getValue()).forEach(ruleString -> {
                        result.addElement(
                            PsiElementUtils.buildSimpleLookupElement(fieldNameOutQuoted + "." + ruleString)
                        );
                    });
                }
            });
        }
    }

    /**
     * Finds the `rules()` method by searching previous siblings of `messages()` method
     *
     * @param method The `messages()` method
     * @return The `rules()` method if found, otherwise null
     */
    private static @Nullable MethodImpl findRulesMethod(MethodImpl method) {
        PsiElement currentElement = method;

        while (currentElement != null) {
            if (currentElement instanceof MethodImpl methodImpl && methodImpl.getName().equals("rules")) {
                return methodImpl;
            }
            currentElement = currentElement.getPrevSibling();
        }

        return null;
    }

    /**
     * Extracts validation rules from an array or a string
     *
     * @param valueElement The value element containing validation rules
     * @return A list of extracted validation rules
     */
    private static List<String> extractValidationRules(PsiElement valueElement) {
        List<String> rulesList = new ArrayList<>();

        if (valueElement instanceof StringLiteralExpression stringLiteral) {
            String[] rules = StrUtils.removeQuotes(stringLiteral.getText()).split("\\|");
            rulesList.addAll(Arrays.asList(rules));
        } else if (valueElement instanceof ArrayCreationExpression valueArray) {
            for (PsiElement child : valueArray.getChildren()) {
                for (PsiElement child2 : child.getChildren()) {
                    if (child2 instanceof StringLiteralExpression childStringLiteral) {
                        rulesList.add(
                            StrUtils.removeQuotes(childStringLiteral.getText())
                        );
                    }
                }
            }
        }

        return rulesList;
    }
}

package at.alirezamoh.idea_whisperer_for_laravel.formRequest;

import at.alirezamoh.idea_whisperer_for_laravel.formRequest.providers.ValidationRuleCompletionProvider;
import at.alirezamoh.idea_whisperer_for_laravel.formRequest.visitors.FormRequestVisitor;
import at.alirezamoh.idea_whisperer_for_laravel.formRequest.visitors.RequestMethodVisitor;
import at.alirezamoh.idea_whisperer_for_laravel.support.IdeaWhispererForLaravelIcon;
import at.alirezamoh.idea_whisperer_for_laravel.support.notification.Notify;
import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.notification.NotificationType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Contributes completion suggestions for Laravel validation rules
 * This class provides code completion for validation rules within Form Request classes
 * and when using the `Validator` facade or the `validate` method on a Request object
 */
public class RuleValidationCompletionContributor extends CompletionContributor {
    /**
     * The namespace of the `Validator` facade
     */
    private final String VALIDATOR_NAMESPACE = "Illuminate\\Support\\Facades\\Validator";

    /**
     * The name of the `make` method in the `Validator` facade
     */
    private final String VALIDATOR_METHOD = "make";


    /**
     * The namespace of the `Request` class
     */
    private final String REQUEST_CLASS_NAMESPACE = "\\Illuminate\\Http\\Request";

    /**
     * The name of the `validate` method in the `Request` class.
     */
    private final String REQUEST_METHOD = "validate";

    /**
     * The path to the JSON file containing the validation rules
     */
    private static final String RULES_PATH = "data/rules.json";


    /**
     * This constructor registers a completion provider that is triggered when the user
     * types a string within a Form Request class, the `Validator` facade, or the `validate`
     * method on a Request object. It checks if the current context is valid for suggesting
     * validation rules and then provides completion suggestions based on the rules defined
     * in the JSON file
     */
    RuleValidationCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withElementType(PhpTokenTypes.tsSTRINGS), new CompletionProvider<>() {

            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                PsiElement psiElement = completionParameters.getPosition();
                int offset = completionParameters.getEditor().getCaretModel().getOffset();

                RequestMethodVisitor requestMethodVisitor = new RequestMethodVisitor(psiElement);
                FormRequestVisitor formRequestVisitor = new FormRequestVisitor(psiElement);
                ValidationRuleCompletionProvider validationRuleCompletionProvider = new ValidationRuleCompletionProvider(psiElement, completionResultSet, completionParameters);

                boolean isInsideRuleMethod = formRequestVisitor.isInsideRuleMethod();
                if (isInsideRequestMethod(requestMethodVisitor, isInsideRuleMethod, psiElement, offset))
                {
                    try {
                        CompletionResultSet resultOnPipe = validationRuleCompletionProvider.getCompletionResultSetOnPipe();
                        if (resultOnPipe != null) {
                            completionResultSet = resultOnPipe;
                        }

                        createLookUpElement(completionResultSet);
                    } catch (IOException e) {
                        Notify.notifyError(psiElement.getProject(), "Failed to load validation rules");
                    }
                }
            }
        });
    }

    /**
     * Checks if the current context is valid for suggesting validation rules
     * @param requestMethodVisitor The visitor for checking method calls
     * @param isInsideRuleMethod   True if the current element is inside the `rules` method of a Form Request
     * @param psiElement           The current PSI element
     * @param offset               The current offset in the editor
     * @return                     True if the context is valid, false otherwise
     */
    private boolean isInsideRequestMethod(RequestMethodVisitor requestMethodVisitor, boolean isInsideRuleMethod, PsiElement psiElement, int offset) {
        return (
            requestMethodVisitor.isInsideRequestMethod(REQUEST_METHOD, REQUEST_CLASS_NAMESPACE)
            || requestMethodVisitor.isInsideValidatorMethod(VALIDATOR_METHOD, VALIDATOR_NAMESPACE)
            || isInsideRuleMethod
        )
        && PsiUtil.isCaretInArrayValue(psiElement, offset)
        && (isInsideRuleMethod || PsiUtil.isCaretInMethodSecParameter(psiElement));
    }

    /**
     * Creates LookupElementBuilder objects for the validation rules and adds them to the result set
     * @param result The completion result set
     * @throws IOException If there is an error loading the validation rules from the JSON file
     */
    private void createLookUpElement(@NotNull CompletionResultSet result) throws IOException {
        for (Map.Entry<String, String> rule : this.loadRules().entrySet()) {
            LookupElementBuilder lookupElement = LookupElementBuilder
                .create(rule.getKey())
                .withIcon(IdeaWhispererForLaravelIcon.LARAVEL_ICON);

            result.addElement(lookupElement);
        }
    }

    /**
     * Loads the validation rules from the JSON file
     * @return A map of rule names and their descriptions
     * @throws IOException If there is an error loading the rules from the file
     */
    public Map<String, String> loadRules() throws IOException {
        Map<String, String> rules = new HashMap<>();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(RULES_PATH);

        if (inputStream != null) {
            String resourceContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject jsonObject = JsonParser.parseString(resourceContent).getAsJsonObject();
            JsonObject allRules = jsonObject.getAsJsonObject("rules");

            for (String key : allRules.keySet()) {
                JsonElement value = allRules.get(key);
                rules.put(key, " = " + value.getAsString());
            }
        }
        return rules;
    }
}

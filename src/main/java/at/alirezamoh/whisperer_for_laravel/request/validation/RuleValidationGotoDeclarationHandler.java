package at.alirezamoh.whisperer_for_laravel.request.validation;

import at.alirezamoh.whisperer_for_laravel.request.validation.util.RuleValidationUtil;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.whisperer_for_laravel.support.utils.DirectoryUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A GotoDeclarationHandler that navigates to the validation rule declaration
 */
public class RuleValidationGotoDeclarationHandler implements GotoDeclarationHandler {

    /**
     * Resolves the target elements for navigating to the validation rule declaration
     *
     * @param sourceElement the source PSI element where the action is triggered
     * @param offset        the caret offset within the editor
     * @param editor        the editor where the action is triggered
     * @return              an array of PSI elements to navigate to, or {@code null} if no targets are found
     */
    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor) {
        if (sourceElement == null) {
            return null;
        }

        Project project = sourceElement.getProject();
        if (!PluginUtils.isLaravelProject(project) && PluginUtils.isLaravelFrameworkNotInstalled(project)) {
            return null;
        }

        if (!(sourceElement.getParent() instanceof StringLiteralExpression stringLiteralExpression)) {
            return null;
        }

        if (!RuleValidationUtil.isInsideCorrectMethod(stringLiteralExpression, project)) {
            return null;
        }

        String originalRuleName = StrUtils.removeQuotes(stringLiteralExpression.getText());
        List<PsiElement> resolvedRules = resolveRules(originalRuleName, project);

        return resolvedRules.isEmpty() ? null : resolvedRules.toArray(new PsiElement[0]);
    }

    @Override
    public @Nullable String getActionText(@NotNull DataContext context) {
        return null;
    }

    /**
     * Resolves the list of provided rules
     *
     * @param originalRuleName the full rule string (e.g., "required|max:255", ["required", "max:255"])
     * @param project          the current project
     * @return a list of resolved rules
     */
    private List<PsiElement> resolveRules(String originalRuleName, Project project) {
        List<PsiElement> resolvedRules = new ArrayList<>();
        String[] rules = originalRuleName.contains("|") ? originalRuleName.split("\\|") : new String[]{originalRuleName};

        for (String rule : rules) {
            String cleanedRule = cleanRuleName(rule);

            if (isKnownRule(cleanedRule)) {
                String finalRule = rule.contains(":") ? rule.split(":")[0] : rule;
                resolvedRules.addAll(findMethodsForRule(finalRule, project));
            }
        }

        return resolvedRules;
    }

    /**
     * Cleans and normalizes a validation rule by removing parameters (after ':') if present
     *
     * @param rule the original rule (e.g., "max:255")
     * @return the cleaned rule (e.g., "max:")
     */
    private String cleanRuleName(String rule) {
        String cleanedRule = rule.contains(":") ? rule.split(":")[0] : rule;
        return rule.contains(":") ? cleanedRule + ":" : cleanedRule;
    }

    /**
     * Checks whether the given rule is a known validation rule in laravel
     *
     * @param rule the cleaned rule name (e.g., "required")
     * @return true or false
     */
    private boolean isKnownRule(String rule) {
        return Arrays.asList(RuleValidationUtil.RULES).contains(rule);
    }

    /**
     * Finds the corresponding validation method for the given rule name in the laravel validation rules file
     *
     * @param ruleName the cleaned rule name (e.g., "required")
     * @param project  the current project
     * @return a list of PSI elements representing the matching methods or an empty list if none are found
     */
    private List<PsiElement> findMethodsForRule(String ruleName, Project project) {
        List<PsiElement> methodsList = new ArrayList<>();
        String methodName = StrUtils.camel("validate_" + ruleName, '_');

        PsiFile validationAttributeFile = DirectoryUtils.getFileByName(project, ProjectDefaultPaths.LARAVEL_VALIDATION_RULES);
        if (validationAttributeFile != null) {
            Collection<Method> methods = PsiTreeUtil.findChildrenOfType(validationAttributeFile, Method.class);

            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    methodsList.add(method);
                }
            }
        }

        return methodsList;
    }
}
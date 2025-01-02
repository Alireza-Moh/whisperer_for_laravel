package at.alirezamoh.whisperer_for_laravel.request.requestField;

import at.alirezamoh.whisperer_for_laravel.request.requestField.util.RequestFieldUtils;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.FieldReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Handles navigation to the declaration of fields defined in FormRequest rules
 */
public class RequestFieldGotoDeclarationHandler implements GotoDeclarationHandler {
    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor) {
        if (sourceElement == null) {
            return null;
        }

        Project project = sourceElement.getProject();
        if (!FrameworkUtils.isLaravelProject(project) && FrameworkUtils.isLaravelFrameworkNotInstalled(project)) {
            return null;
        }

        PsiElement parentElement = resolveParentElement(sourceElement);
        if (parentElement instanceof VariableImpl variable) {
            return findDeclarationTargets(variable, project, sourceElement.getParent());
        }

        return null;
    }

    @Override
    public @Nullable String getActionText(@NotNull DataContext context) {
        return null;
    }

    private PsiElement resolveParentElement(PsiElement sourceElement) {
        PsiElement parent = sourceElement.getParent().getParent().getParent();
        if (parent instanceof ArrayAccessExpression arrayAccessExpression) {
            return arrayAccessExpression.getValue();
        }

        parent = sourceElement.getParent();
        return parent instanceof FieldReferenceImpl fieldReference ? fieldReference.getClassReference() : null;
    }

    private PsiElement[] findDeclarationTargets(VariableImpl variable, Project project, PsiElement contextElement) {
        PhpClassImpl phpClass = RequestFieldUtils.resolveRequestClass(variable, project);
        if (phpClass == null) {
            return null;
        }

        Collection<ArrayHashElement> rules = RequestFieldUtils.resolveRulesFromVariable(variable, project, contextElement);

        return rules == null
            ? null
            : rules.stream()
                .filter(rule -> RequestFieldUtils.isMatchingRule(contextElement, rule))
                .map(ArrayHashElement::getKey)
                .toArray(PsiElement[]::new);
    }
}
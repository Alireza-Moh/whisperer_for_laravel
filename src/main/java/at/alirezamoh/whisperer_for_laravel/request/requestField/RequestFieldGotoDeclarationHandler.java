package at.alirezamoh.whisperer_for_laravel.request.requestField;

import at.alirezamoh.whisperer_for_laravel.request.requestField.util.RequestFieldUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
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
        if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
            return null;
        }

        PsiElement parentElement = resolveParentElement(sourceElement);
        if (parentElement instanceof VariableImpl variable) {
            return findDeclarationTargets(variable, project, sourceElement.getParent());
        }
        else if (parentElement instanceof MethodReference methodRef) {
            return findDeclarationTargets(methodRef, project, sourceElement.getParent());
        }

        return null;
    }

    @Override
    public @Nullable String getActionText(@NotNull DataContext context) {
        return null;
    }

    private PsiElement resolveParentElement(PsiElement sourceElement) {
        PsiElement greatGrandParent = RequestFieldUtils.getNthParent(sourceElement, 3);
        if (greatGrandParent == null) {
            return null;
        }

        if (greatGrandParent instanceof ArrayAccessExpression arrayAccessExpression) {
            return arrayAccessExpression.getValue();
        }

        PsiElement parent = sourceElement.getParent();
        if (parent instanceof FieldReferenceImpl fieldReference) {
            return fieldReference.getClassReference();
        }

        if (RequestFieldUtils.isInsideCorrectMethod(sourceElement, parent.getProject())) {
            MethodReference methodReference = MethodUtils.resolveMethodReference(sourceElement, 10);

            if (methodReference != null) {
                return methodReference.getClassReference();
            }
            return null;
        }

        return null;
    }

    private @Nullable PsiElement[] findDeclarationTargets(PsiElement element, Project project, PsiElement contextElement) {
        PhpClassImpl phpClass = RequestFieldUtils.resolvePhpClass(element, project);

        if (phpClass == null) {
            return null;
        }

        Collection<ArrayHashElement> rules = RequestFieldUtils.resolveRulesFromVariable(phpClass, project, contextElement);

        return rules == null
            ? null
            : rules.stream()
                .filter(rule -> RequestFieldUtils.isMatchingRule(contextElement, rule))
                .map(ArrayHashElement::getKey)
                .toArray(PsiElement[]::new);
    }
}
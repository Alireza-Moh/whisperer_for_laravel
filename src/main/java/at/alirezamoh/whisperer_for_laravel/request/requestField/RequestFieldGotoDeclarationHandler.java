package at.alirezamoh.whisperer_for_laravel.request.requestField;

import at.alirezamoh.whisperer_for_laravel.request.requestField.util.RequestFieldUtils;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.FieldReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl;
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

        if (!(sourceElement.getParent() instanceof FieldReferenceImpl fieldReference)) {
            return null;
        }

        VariableImpl variable = (VariableImpl) fieldReference.getClassReference();
        PhpClassImpl phpClass = RequestFieldUtils.resolveRequestClass(variable, project);

        if (phpClass == null) {
            return null;
        }

        Collection<ArrayHashElement> rules = RequestFieldUtils.getRules(phpClass, project);
        if (rules == null && "\\Illuminate\\Http\\Request".equals(phpClass.getFQN())) {
            MethodImpl method = PsiTreeUtil.getParentOfType(fieldReference, MethodImpl.class);
            if (method != null) {
                rules = RequestFieldUtils.extractValidationRulesFromMethod(method);
            }
        }

        if (rules != null) {
            return rules.stream()
                .filter(rule -> RequestFieldUtils.isMatchingRule(fieldReference, rule))
                .map(ArrayHashElement::getKey)
                .toArray(PsiElement[]::new);
        }
        return null;
    }

    @Override
    public @Nullable String getActionText(@NotNull DataContext context) {
        return null;
    }
}
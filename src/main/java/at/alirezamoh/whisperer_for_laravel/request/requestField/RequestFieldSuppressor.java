package at.alirezamoh.whisperer_for_laravel.request.requestField;

import at.alirezamoh.whisperer_for_laravel.request.requestField.util.RequestFieldUtils;
import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpDynamicFieldDeclarationInspection;
import com.jetbrains.php.lang.inspections.PhpUndefinedFieldInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Suppresses inspections for fields defined in FormRequest rules
 * It suppresses "Property accessed via magic method" warning
 */
public class RequestFieldSuppressor implements InspectionSuppressor {
    private final List<String> suppressedPhpInspections = List.of(
        new PhpUndefinedFieldInspection().getID(),
        new PhpDynamicFieldDeclarationInspection().getID()
    );

    @Override
    public boolean isSuppressedFor(@NotNull PsiElement psiElement, @NotNull String s) {
        if (!suppressedPhpInspections.contains(s)) {
            return false;
        }

        if (!(psiElement.getParent() instanceof FieldReference fieldReference)) {
            return false;
        }

        return isFieldInFormRequestRules(fieldReference);
    }

    @Override
    public SuppressQuickFix @NotNull [] getSuppressActions(@Nullable PsiElement psiElement, @NotNull String s) {
        return new SuppressQuickFix[0];
    }

    /**
     * Checks if the given field reference matches a field defined in FormRequest rules
     *
     * @param fieldReference the field reference to check
     * @return true or false
     */
    private boolean isFieldInFormRequestRules(FieldReference fieldReference) {
        PsiElement ref = fieldReference.getClassReference();
        Project project = fieldReference.getProject();

        if (ref == null) {
            return false;
        }

        if (!(ref instanceof VariableImpl variable)) {
            return false;
        }

        PhpClassImpl phpClass = RequestFieldUtils.resolveRequestClass(variable, project);
        if (phpClass == null) {
            return false;
        }

        Collection<ArrayHashElement> rules = RequestFieldUtils.getRules(phpClass, project);
        if (rules != null) {
            return rules.stream().anyMatch(rule -> RequestFieldUtils.isMatchingRule(fieldReference, rule));
        }

        if (RequestFieldUtils.REQUEST.equals(phpClass.getFQN())) {
            MethodImpl method = PsiTreeUtil.getParentOfType(fieldReference, MethodImpl.class);
            if (method != null) {
                rules = RequestFieldUtils.extractValidationRulesFromMethod(method);
                return rules.stream().anyMatch(rule -> RequestFieldUtils.isMatchingRule(fieldReference, rule));
            }
        }


        return false;
    }
}

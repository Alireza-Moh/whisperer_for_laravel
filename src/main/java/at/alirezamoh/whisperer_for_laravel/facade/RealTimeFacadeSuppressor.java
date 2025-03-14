package at.alirezamoh.whisperer_for_laravel.facade;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.inspections.PhpUndefinedClassInspection;
import com.jetbrains.php.lang.inspections.PhpUndefinedNamespaceInspection;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Suppresses inspections for classes that are intended to be used as facades
 * It suppresses "Undefined class..." and "Undefined namespace..." warning
 */
public class RealTimeFacadeSuppressor implements InspectionSuppressor {
    private final List<String> suppressedPhpInspections = List.of(
        new PhpUndefinedClassInspection().getID(),
        new PhpUndefinedNamespaceInspection().getID()
    );

    @Override
    public boolean isSuppressedFor(@NotNull PsiElement psiElement, @NotNull String s) {
        Project project = psiElement.getProject();
        if (PluginUtils.shouldNotCompleteOrNavigate(project) || !suppressedPhpInspections.contains(s)) {
            return false;
        }

        SettingsState settingsState = SettingsState.getInstance(project);
        if (!settingsState.isSuppressRealTimeFacadeWarnings()) {
            return false;
        }

        ClassReference classReference = PsiTreeUtil.getParentOfType(psiElement, ClassReference.class);
        if (classReference == null) {
            return false;
        }

        return isFacadeClass(classReference);
    }

    @Override
    public SuppressQuickFix @NotNull [] getSuppressActions(@Nullable PsiElement psiElement, @NotNull String s) {
        return new SuppressQuickFix[0];
    }

    private boolean isFacadeClass(ClassReference classReference) {
        return Objects.requireNonNull(classReference.getFQN()).startsWith("\\Facades\\");
    }
}

package at.alirezamoh.whisperer_for_laravel.packages.livewire.htmlDirectives.supressor;

import at.alirezamoh.whisperer_for_laravel.packages.livewire.LivewireUtil;
import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Suppresses inspections for livewire html directives
 * It suppresses "Attribute [directive name] is not allowed here" warning
 */
public class LivewireHtmlDirectiveSuppressor implements InspectionSuppressor {
    private final List<String> suppressedXMLInspections = List.of(
        "HtmlUnknownAttribute"
    );

    @Override
    public boolean isSuppressedFor(@NotNull PsiElement psiElement, @NotNull String s) {
        Project project = psiElement.getProject();

        if (LivewireUtil.doNotCompleteOrNavigate(project) && !suppressedXMLInspections.contains(s)) {
            return false;
        }

        PsiElement parent = psiElement.getParent();

        return parent instanceof XmlAttribute xmlAttribute
            && xmlAttribute.getName().startsWith("wire:")
            && suppressedXMLInspections.contains(s);
    }

    @Override
    public SuppressQuickFix @NotNull [] getSuppressActions(@Nullable PsiElement psiElement, @NotNull String s) {
        return new SuppressQuickFix[0];
    }
}

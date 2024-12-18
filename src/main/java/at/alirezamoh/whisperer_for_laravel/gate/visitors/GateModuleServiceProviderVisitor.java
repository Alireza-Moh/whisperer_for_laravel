package at.alirezamoh.whisperer_for_laravel.gate.visitors;

import at.alirezamoh.whisperer_for_laravel.gate.util.GateUtil;
import at.alirezamoh.whisperer_for_laravel.support.WhispererForLaravelIcon;
import at.alirezamoh.whisperer_for_laravel.support.applicationModules.visitors.BaseServiceProviderVisitor;
import at.alirezamoh.whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GateModuleServiceProviderVisitor extends BaseServiceProviderVisitor {
    private List<LookupElementBuilder> variants = new ArrayList<>();

    /**
     * @param project The current project
     */
    public GateModuleServiceProviderVisitor(Project project) {
        super(project);
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {
        if (element instanceof MethodReference methodReference) {
            if (GateUtil.isGateFacadeMethod(methodReference, project)) {
                this.getAbilities(methodReference);
            }
        }
        super.visitElement(element);
    }

    public List<LookupElementBuilder> getVariants() {
        return variants;
    }

    private void getAbilities(MethodReference method) {
        String ability = PsiUtil.getFirstParameterFromMethod(method);

        if (ability != null) {
            variants.add(
                LookupElementBuilder.create(ability).withIcon(WhispererForLaravelIcon.LARAVEL_ICON)
            );
        }
    }
}

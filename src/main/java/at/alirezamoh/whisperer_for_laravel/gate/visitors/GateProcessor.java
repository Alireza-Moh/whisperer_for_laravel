package at.alirezamoh.whisperer_for_laravel.gate.visitors;

import at.alirezamoh.whisperer_for_laravel.support.applicationModules.visitors.BaseServiceProviderVisitor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;

import java.util.List;

public class GateProcessor {
    private final Project project;

    public GateProcessor(Project project) {
        this.project = project;
    }

    public List<String> collectGates() {
        GateModuleServiceProviderVisitor visitor = new GateModuleServiceProviderVisitor(project);
        traverseAndAccept(visitor);

        return visitor.getVariants();
    }

    public PsiElement findGateAbility(PsiElement element) {
        GateAbilityFinder visitor = new GateAbilityFinder(element);
        traverseAndAccept(visitor);

        return visitor.getFoundedAbility();
    }

    private void traverseAndAccept(PsiElementVisitor visitor) {
        for (PsiFile serviceProvider : BaseServiceProviderVisitor.getProviders(project)) {
            serviceProvider.acceptChildren(visitor);
        }
    }
}

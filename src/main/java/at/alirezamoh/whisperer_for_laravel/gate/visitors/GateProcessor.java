package at.alirezamoh.whisperer_for_laravel.gate.visitors;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.applicationModules.visitors.BaseServiceProviderVisitor;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.List;

public class GateProcessor {
    private final Project project;

    private final SettingsState projectSettingsState;

    public GateProcessor(Project project) {
        this.project = project;
        this.projectSettingsState = SettingsState.getInstance(project);
    }

    public List<LookupElementBuilder> collectGates() {
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
        for (PhpClass serviceProvider : BaseServiceProviderVisitor.getProviders(project)) {
            serviceProvider.acceptChildren(visitor);
        }
    }
}

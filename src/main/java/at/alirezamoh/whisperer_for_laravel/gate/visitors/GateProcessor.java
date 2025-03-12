package at.alirezamoh.whisperer_for_laravel.gate.visitors;

import at.alirezamoh.whisperer_for_laravel.indexes.PolicyIndex;
import at.alirezamoh.whisperer_for_laravel.support.applicationModules.visitors.BaseServiceProviderVisitor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.IdFilter;

import java.util.ArrayList;
import java.util.List;

public class GateProcessor {
    private final Project project;

    public GateProcessor(Project project) {
        this.project = project;
    }

    public List<String> collectGates() {
        GateModuleServiceProviderVisitor visitor = new GateModuleServiceProviderVisitor(project);
        traverseAndAccept(visitor);

        List<String> variants = visitor.getVariants();

        variants.addAll(collectPolicies(project));

        return variants;
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

    private List<String> collectPolicies(Project project) {
        List<String> policies = new ArrayList<>();
        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();

        fileBasedIndex.processAllKeys(PolicyIndex.INDEX_ID, key -> {
                fileBasedIndex.processValues(
                    PolicyIndex.INDEX_ID,
                    key,
                    null,
                    (file, values) -> {
                        policies.addAll(values);
                        return true;
                    },
                    GlobalSearchScope.projectScope(project),
                    IdFilter.getProjectIdFilter(project, false)
                );
                return true;
            },
            GlobalSearchScope.projectScope(project),
            IdFilter.getProjectIdFilter(project, false)
        );

        return policies;
    }
}

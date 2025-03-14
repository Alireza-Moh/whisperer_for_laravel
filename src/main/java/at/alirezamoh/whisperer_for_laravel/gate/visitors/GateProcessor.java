package at.alirezamoh.whisperer_for_laravel.gate.visitors;

import at.alirezamoh.whisperer_for_laravel.indexes.PolicyIndex;
import at.alirezamoh.whisperer_for_laravel.support.applicationModules.visitors.BaseServiceProviderVisitor;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.IdFilter;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.ArrayList;
import java.util.List;

public class GateProcessor {
    private final Project project;

    public GateProcessor(Project project) {
        this.project = project;
    }

    public List<String> collectGates(boolean shouldSearchForPolicy) {
        GateModuleServiceProviderVisitor visitor = new GateModuleServiceProviderVisitor(project);
        traverseAndAccept(visitor);

        List<String> variants = visitor.getVariants();

        if (shouldSearchForPolicy) {
            variants.addAll(collectPolicies(project));
        }

        return variants;
    }

    public List<PsiElement> findGateAbility(PsiElement element, boolean shouldSearchForPolicy) {
        String target = StrUtils.removeQuotes(element.getText());
        List<PsiElement> variants = new ArrayList<>();
        GateAbilityFinder visitor = new GateAbilityFinder(element);
        traverseAndAccept(visitor);

        PsiElement foundedElement = visitor.getFoundedAbility();
        if (foundedElement == null && shouldSearchForPolicy) {
            FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();

            fileBasedIndex.processAllKeys(PolicyIndex.INDEX_ID, key -> {
                    fileBasedIndex.processValues(
                        PolicyIndex.INDEX_ID,
                        key,
                        null,
                        (file, values) -> {
                            for (String value : values) {
                                String[] parts = value.split("\\|");
                                if (parts.length != 2) {
                                    continue;
                                }

                                if (parts[0].equals(target)) {
                                    PhpClass policyClass = PhpClassUtils.getClassByFQN(project, key);
                                    if (policyClass != null) {
                                        Method method = policyClass.findMethodByName(parts[1]);
                                        if (method != null) {
                                            variants.add(method);
                                        }
                                    }
                                }
                            }
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
        }

        if (foundedElement != null) {
            variants.add(foundedElement);
        }

        return variants;
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
                        for (String value : values) {
                            String[] splitValue = value.split("\\|");

                            if (splitValue.length > 0) {
                                policies.add(splitValue[0]);
                            }
                        }
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

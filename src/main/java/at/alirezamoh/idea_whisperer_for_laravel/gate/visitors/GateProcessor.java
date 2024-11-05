package at.alirezamoh.idea_whisperer_for_laravel.gate.visitors;

import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.applicationModules.utils.ApplicationModuleUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;

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
        PsiFile appServiceProviderFile = null;

        if (projectSettingsState.isModuleApplication()) {
            String rootAppPath = projectSettingsState.getRootAppPath();
            if (rootAppPath != null) {
                appServiceProviderFile = DirectoryPsiUtil.getFileByName(
                    project,
                    projectSettingsState.replaceAndSlashes(rootAppPath)
                    + ProjectDefaultPaths.APP_SERVICE_PROVIDER_PATH
                );
            }
        }
        else {
            appServiceProviderFile = DirectoryPsiUtil.getFileByName(
                project,
                "/app/" + ProjectDefaultPaths.APP_SERVICE_PROVIDER_PATH
            );
        }

        if (appServiceProviderFile != null) {
            appServiceProviderFile.acceptChildren(visitor);
        }

        searchInModules(visitor);
    }

    private void searchInModules(PsiElementVisitor visitor) {
        String moduleDirectoryRootPath = projectSettingsState.getFormattedModuleRootDirectoryPath();

        if (projectSettingsState.isModuleApplication() && moduleDirectoryRootPath != null) {
            for (PsiFile provider : ApplicationModuleUtil.getProviders(project)) {
                provider.acceptChildren(visitor);
            }
        }
    }
}

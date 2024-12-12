package at.alirezamoh.idea_whisperer_for_laravel.gate.visitors;

import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.applicationModules.utils.ApplicationModuleUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
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
        String defaultPath = "";
        if (!projectSettingsState.isDefaultLaravelDirectoryEmpty()) {
            defaultPath = StrUtil.addSlashes(
                projectSettingsState.getLaravelDirectoryPath(),
                false,
                true
            ) + ProjectDefaultPaths.APP_SERVICE_PROVIDER_PATH;
        }

        PsiFile appServiceProviderFile = DirectoryPsiUtil.getFileByName(project, defaultPath);

        if (appServiceProviderFile != null) {
            appServiceProviderFile.acceptChildren(visitor);
        }

        String moduleDirectoryRootPath = projectSettingsState.getFormattedModulesDirectoryPath();
        if (projectSettingsState.isModuleApplication() && moduleDirectoryRootPath != null) {
            PsiDirectory modulesDir = DirectoryPsiUtil.getDirectory(project, moduleDirectoryRootPath);

            if (modulesDir != null) {
                for (PsiDirectory moduleDir : modulesDir.getSubdirectories()) {
                    for (PhpClass serviceProvider : ApplicationModuleUtil.getProviders(moduleDir)) {
                        serviceProvider.acceptChildren(visitor);
                    }
                }
            }
        }
    }
}

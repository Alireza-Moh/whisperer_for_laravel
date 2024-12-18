package at.alirezamoh.whisperer_for_laravel.blade.visitors;

import at.alirezamoh.whisperer_for_laravel.blade.BladeModule;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.whisperer_for_laravel.support.applicationModules.visitors.BaseServiceProviderVisitor;
import at.alirezamoh.whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.whisperer_for_laravel.support.psiUtil.PsiUtil;
import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.*;

public class BladeFileCollector {
    /**
     * List of the blade files
     */
    private List<LookupElementBuilder> variants = new ArrayList<>();

    /**
     * Map of PsiFile and their corresponding bladeFile name
     */
    private Map<PsiFile, String> bladeFilesWithCorrectPsiFile = new HashMap<>();

    /**
     * The current project
     */
    private Project project;

    /**
     * The project settings
     */
    private SettingsState projectSettingState;

    /**
     * Should save the blade file psiFile
     */
    private boolean withPsiFile;

    public BladeFileCollector(Project project) {
        this.project = project;
        this.projectSettingState = SettingsState.getInstance(project);
    }

    public BladeFileCollector startSearching() {
        String defaultViewPath = ProjectDefaultPaths.VIEW_PATH;
        if (!projectSettingState.isLaravelDirectoryEmpty()) {
            defaultViewPath = StrUtil.addSlashes(
                projectSettingState.getLaravelDirectoryPath(),
                false,
                true
            ) + ProjectDefaultPaths.VIEW_PATH;
        }

        PsiDirectory defaultResDir = DirectoryPsiUtil.getDirectory(project, defaultViewPath);

        if (defaultResDir != null) {
            searchForBladeFiles(defaultResDir, "", "");
        }

        searchForBladeFilesInModules();

        return this;
    }

    public List<LookupElementBuilder> getVariants() {
        return variants;
    }

    public Map<PsiFile, String> getBladeFilesWithCorrectPsiFile() {
        return bladeFilesWithCorrectPsiFile;
    }

    public void setWithPsiFile(boolean withPsiFile) {
        this.withPsiFile = withPsiFile;
    }

    /**
     * Recursively searches for Blade files within a directory
     * @param directory     The directory to search in
     * @param currentPath   The current path being traversed
     * @param viewNamespace The view namespace to prepend to the file name
     */
    public void searchForBladeFiles(PsiDirectory directory, String currentPath, String viewNamespace) {
        for (PsiFile file : directory.getFiles()) {
            if (file.getFileType() instanceof BladeFileType) {
                String fileName = file.getName().replace(".blade.php", "");
                String finalFileName = currentPath.isEmpty() ? fileName : currentPath + "." + fileName;

                if (!viewNamespace.isEmpty()) {
                    finalFileName = viewNamespace + "::" + finalFileName;
                }

                if (withPsiFile) {
                    bladeFilesWithCorrectPsiFile.put(file, finalFileName);
                }
                else {
                    variants.add(PsiUtil.buildSimpleLookupElement(finalFileName));
                }
            }
        }

        for (PsiDirectory subdirectory : directory.getSubdirectories()) {
            String newPath = currentPath.isEmpty()
                ? subdirectory.getName()
                : currentPath + "." + subdirectory.getName();

            searchForBladeFiles(subdirectory, newPath, viewNamespace);
        }
    }

    /**
     * Searches for bladeFiles within module
     */
    private void searchForBladeFilesInModules() {
        BladeModuleServiceProviderVisitor bladeModuleServiceProviderVisitor = new BladeModuleServiceProviderVisitor(project);

        for (PhpClass serviceProvider : BaseServiceProviderVisitor.getProviders(project)) {
            serviceProvider.acceptChildren(bladeModuleServiceProviderVisitor);
            List<BladeModule> bladeModules = bladeModuleServiceProviderVisitor.getBladeFilesInModule();

            if (!bladeModules.isEmpty()) {
                for (BladeModule bladeModule : bladeModules) {
                    searchForBladeFiles(bladeModule.bladeDir(), "", bladeModule.viewNamespace());
                }
            }
        }
    }
}

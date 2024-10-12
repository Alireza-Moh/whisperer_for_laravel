package at.alirezamoh.idea_whisperer_for_laravel.blade.visitors;

import at.alirezamoh.idea_whisperer_for_laravel.blade.BladeModule;
import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.IdeaWhispererForLaravelIcon;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.applicationModules.utils.ApplicationModuleUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.blade.BladeFileType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        PsiDirectory defaultViewDir = DirectoryPsiUtil.getDirectory(this.project, ProjectDefaultPaths.VIEW_PATH);
        if (defaultViewDir != null) {
            searchForBladeFiles(defaultViewDir, "", "");
        }

        String moduleDirectoryRootPath = StrUtil.addSlashes(
            projectSettingState.getModuleRootDirectoryPath(),
            false,
            true
        );
        if (projectSettingState.isModuleApplication() && moduleDirectoryRootPath != null) {
            searchForBladeFilesInModules();
        }

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
        if (directory.getName().equals("components")) {
            return;
        }

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
                    variants.add(buildLookupElement(finalFileName));
                }
            }
        }

        for (PsiDirectory subdirectory : directory.getSubdirectories()) {
            String newPath = currentPath.isEmpty() ? subdirectory.getName() : currentPath + "." + subdirectory.getName();
            searchForBladeFiles(subdirectory, newPath, viewNamespace);
        }
    }

    /**
     * Searches for bladeFiles within module
     */
    private void searchForBladeFilesInModules() {
        BladeModuleServiceProviderVisitor bladeModuleServiceProviderVisitor = new BladeModuleServiceProviderVisitor(project);

        for (PsiFile serviceProviderFile : ApplicationModuleUtil.getProviders(project)) {
            serviceProviderFile.acceptChildren(bladeModuleServiceProviderVisitor);

            List<BladeModule> bladeModules = bladeModuleServiceProviderVisitor.getBladeFilesInModule();

            if (!bladeModules.isEmpty()) {
                for (BladeModule bladeModule : bladeModules) {
                    searchForBladeFiles(bladeModule.bladeDir(), "", bladeModule.viewNamespace());
                }
            }
        }
    }

    /**
     * Builds a LookupElementBuilder with the Laravel icon
     * @param element The element to create the LookupElementBuilder for
     * @return        The LookupElement
     */
    public LookupElementBuilder buildLookupElement(@NotNull String element) {
        return LookupElementBuilder.create(element).withIcon(IdeaWhispererForLaravelIcon.LARAVEL_ICON);
    }
}

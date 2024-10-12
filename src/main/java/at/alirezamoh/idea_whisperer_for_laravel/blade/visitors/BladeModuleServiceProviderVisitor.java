package at.alirezamoh.idea_whisperer_for_laravel.blade.visitors;

import at.alirezamoh.idea_whisperer_for_laravel.blade.BladeModule;
import at.alirezamoh.idea_whisperer_for_laravel.support.applicationModules.visitors.BaseServiceProviderVisitor;
import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Visits a laravel service provider in a module-based Laravel application
 */
public class BladeModuleServiceProviderVisitor extends BaseServiceProviderVisitor {
    /**
     * The name of the method to look for in the service provider
     */
    private final String LOAD_VIEWS_METHOD = "loadViewsFrom";

    /**
     * A map of Blade files and their corresponding names
     */
    private List<BladeModule> bladeFilesInModule = new ArrayList<>();

    /**
     * @param project The current project
     */
    public BladeModuleServiceProviderVisitor(Project project) {
        super(project);
    }

    /**
     * Visits an element in the laravel service provider
     * @param element The PSI element being visited
     */
    @Override
    public void visitElement(@NotNull PsiElement element) {
        if (element instanceof MethodReference methodReference) {
            String methodName = methodReference.getName();
            if (methodName != null && methodName.equals(LOAD_VIEWS_METHOD)) {
                this.initParameters(methodReference);
            }
        }
        super.visitElement(element);
    }

    /**
     * Returns a map of Blade files and their corresponding names
     * @return Blade files
     */
    public List<BladeModule> getBladeFilesInModule() {
        return bladeFilesInModule;
    }

    /**
     * Iterates over the modules in the project and searches for Blade files
     * @param method The method reference being visited
     */
    private void initParameters(MethodReference method) {
        String viewNamespace = PsiUtil.getSecondParameterFromMethod(method);
        String viewDirName = PsiUtil.getFirstParameterFromMethod(method);

        if (viewNamespace == null || viewDirName == null) {
            return;
        }

        if (moduleRootDirectoryPath != null) {
            viewDirName = StrUtil.getLastWord(viewDirName);
            for (PsiDirectory module : moduleRootDirectoryPath.getSubdirectories()) {
                PsiDirectory resourcesDir = module.findSubdirectory("resources");

                if (resourcesDir != null) {
                    PsiDirectory viewsDir = resourcesDir.findSubdirectory("views");
                    if (viewsDir != null) {
                        PsiDirectory finalBladeDir = findBladeDir(viewsDir, viewDirName);

                        if (finalBladeDir != null) {
                            bladeFilesInModule.add(new BladeModule(viewNamespace, finalBladeDir));
                        }
                    }
                }

            }
        }
    }

    private @Nullable PsiDirectory findBladeDir(PsiDirectory currentDir, String bladeDirName) {
        if (currentDir.getName().equals(bladeDirName)) {
            return currentDir;
        }

        for (PsiDirectory subDir : currentDir.getSubdirectories()) {
            PsiDirectory foundDir = findBladeDir(subDir, bladeDirName);
            if (foundDir != null) {
                return foundDir;
            }
        }

        return null;
    }
}

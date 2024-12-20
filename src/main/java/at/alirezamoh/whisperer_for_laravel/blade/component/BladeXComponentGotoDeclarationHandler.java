package at.alirezamoh.whisperer_for_laravel.blade.component;

import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A GotoDeclarationHandler that navigates to the Blade component
 * Go to Declaration on a `x-...` Blade component tag
 */
public class BladeXComponentGotoDeclarationHandler implements GotoDeclarationHandler {

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor) {
        if (sourceElement == null) {
            return null;
        }

        Project project = sourceElement.getProject();

        if (!FrameworkUtils.isLaravelProject(project) && FrameworkUtils.isLaravelFrameworkNotInstalled(project)) {
            return null;
        }

        String text = sourceElement.getText();
        if (text == null || !text.startsWith("x-")) {
            return null;
        }

        String componentName = text.substring(2);

        // Convert component name into a relative file path: "admin.dashboard.alert" -> "admin/dashboard/alert.blade.php"
        String relativePath = componentName.replace('.', '/') + ".blade.php";

        // e.g. "admin/dashboard/alert.blade.php" -> "alert.blade.php"
        String fileName = relativePath.substring(relativePath.lastIndexOf('/') + 1);

        Collection<VirtualFile> candidateFiles = FilenameIndex.getVirtualFilesByName(
            fileName,
            GlobalSearchScope.projectScope(project)
        );

        PsiManager psiManager = PsiManager.getInstance(project);
        Collection<PsiElement> targets = new ArrayList<>(findComponentPhpClass(componentName, project));

        for (VirtualFile file : candidateFiles) {
            String fullPath = file.getPath().replace("\\", "/");
            if (fullPath.contains("components/") && fullPath.endsWith(relativePath)) {
                PsiFile psiFile = psiManager.findFile(file);
                if (psiFile != null && psiFile.getFileType() instanceof BladeFileType) {
                    targets.add(psiFile);
                }
            }
        }

        return targets.isEmpty() ? null : targets.toArray(new PsiElement[0]);
    }

    @Override
    public @Nullable String getActionText(@NotNull DataContext context) {
        return null;
    }

    private Collection<PhpClass> findComponentPhpClass(String componentName, Project project) {
        String componentNameInCamelCase = StrUtil.camel(componentName);

        return PhpIndex.getInstance(project).getClassesByName(componentNameInCamelCase);
    }
}
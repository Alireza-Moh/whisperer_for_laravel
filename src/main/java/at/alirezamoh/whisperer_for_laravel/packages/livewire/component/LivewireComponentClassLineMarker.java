package at.alirezamoh.whisperer_for_laravel.packages.livewire.component;

import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LivewireComponentClassLineMarker extends RelatedItemLineMarkerProvider {
    /**
     * Collects navigation markers for a given PSI element if it represents a livewire component as PHP class
     *
     * @param element the PSI element to inspect
     * @param result  the collection to add marker info to
     */
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        Project project = element.getProject();
        PhpClass baseLivewireComponent = PhpClassUtils.getClassByFQN(project, "\\Livewire\\Component");
        if (
            element instanceof PhpClassImpl phpClassImpl
            && baseLivewireComponent != null
            && PhpClassUtils.isChildOf(phpClassImpl, baseLivewireComponent)
        ) {
            PsiElement classNameIdentifier = phpClassImpl.getNameIdentifier();
            if (classNameIdentifier == null) {
                return;
            }

            LivewireLineMarkerUtil.createLineMarker(
                result,
                collectViewFilesForComponent(project, phpClassImpl.getName()),
                classNameIdentifier
            );
        }
    }

    /**
     * Collects all Blade view files that match the given Livewire component name
     * This method searches for Blade files using both snake-case and PascalCase variants
     * of the component name (e.g., {@code my-component.blade.php}, {@code MyComponent.blade.php}),
     * and filters them to include only those located within a "livewire" directory.
     *
     * @param project       the current project
     * @param componentName the name of the Livewire component (e.g., "MyComponent")
     * @return a list of matching Blade {@link PsiFile} instances
     */
    private List<PsiFile> collectViewFilesForComponent(Project project, String componentName) {
        List<PsiFile> files = new ArrayList<>();

        String possibleBladeFileName = StrUtils.snake(componentName, "-") + ".blade.php";
        Collection<VirtualFile> candidateFiles = FilenameIndex.getVirtualFilesByName(
            possibleBladeFileName,
            GlobalSearchScope.projectScope(project)
        );

        candidateFiles.addAll(
            FilenameIndex.getVirtualFilesByName(
                componentName + ".blade.php",
                GlobalSearchScope.projectScope(project)
            )
        );

        PsiManager psiManager = PsiManager.getInstance(project);

        for (VirtualFile file : candidateFiles) {
            if (file.getPath().contains("livewire")) {
                PsiFile psiFile = psiManager.findFile(file);
                if (psiFile != null && psiFile.getFileType() instanceof BladeFileType) {
                    files.add(psiFile);
                }
            }
        }

        return files;
    }
}

package at.alirezamoh.whisperer_for_laravel.packages.livewire.component;

import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.blade.html.BladeHtmlFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LivewireComponentBladeFileLineMarker extends RelatedItemLineMarkerProvider {
    /**
     * Collects navigation markers for a given PSI element if it represents a livewire view(blade) file
     *
     * @param element the PSI element to inspect
     * @param result  the collection to add marker info to
     */
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        Project project = element.getProject();

        if (!(element instanceof PsiFile file)) {
            return;
        }

        FileType fileType = file.getFileType();
        if ((fileType instanceof BladeHtmlFileType || fileType instanceof BladeFileType) && file.getVirtualFile().getPath().contains("livewire")) {
            LivewireLineMarkerUtil.createLineMarker(
                result,
                collectComponentClassForBladeFile(project, file.getName()),
                file.getFirstChild()
            );
        }
    }

    /**
     * Finds all PHP class files that are potential Livewire components associated with a given Blade view file.
     * The method derives the possible class name from the Blade file name by converting it to PascalCase,
     * then searches for PHP files with that name. It filters results to include only classes that extend
     * {@code \Livewire\Component}.
     *
     * @param project  the current project
     * @param fileName the Blade view file name (e.g., {@code my-component.blade.php})
     * @return a list of matching PHP {@link PsiFile} instances representing Livewire component classes
     */
    private List<PsiFile> collectComponentClassForBladeFile(Project project, String fileName) {
        List<PsiFile> files = new ArrayList<>();

        String fileNameWithoutExtension = fileName.replace(".blade.php", "");
        String possibleComponentFileName = "";
        if (fileName.contains("-")) {
            possibleComponentFileName = StrUtils.ucFirst(StrUtils.camel(fileNameWithoutExtension, '-')) + ".php";
        }
        else {
            possibleComponentFileName = StrUtils.ucFirst(fileNameWithoutExtension) + ".php";
        }

        Collection<VirtualFile> candidateFiles = FilenameIndex.getVirtualFilesByName(
            possibleComponentFileName,
            GlobalSearchScope.projectScope(project)
        );

        PsiManager psiManager = PsiManager.getInstance(project);

        PhpClass baseLivewireComponent = PhpClassUtils.getClassByFQN(project, "\\Livewire\\Component");
        for (VirtualFile file : candidateFiles) {
            PsiFile psiFile = psiManager.findFile(file);
            if (psiFile instanceof PhpFile phpFile) {
                PhpClass phpClass = PhpClassUtils.getPhpClassFromFile(phpFile, possibleComponentFileName.replace(".php", ""));
                if (
                    phpClass instanceof PhpClassImpl phpClassImpl
                    && baseLivewireComponent != null
                    && PhpClassUtils.isChildOf(phpClassImpl, baseLivewireComponent)
                ) {
                    files.add(psiFile);
                }
            }
        }

        return files;
    }
}

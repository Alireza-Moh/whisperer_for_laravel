package at.alirezamoh.idea_whisperer_for_laravel.inertia;

import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InertiaReference extends PsiReferenceBase<PsiElement> {
    /**
     * The current project
     */
    private Project project;

    /**
     * @param element        The PSI element being referenced
     * @param rangeInElement The text range of the reference within the element
     */
    public InertiaReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);

        this.project = element.getProject();
    }

    @Override
    public @Nullable PsiElement resolve() {
        String text = StrUtil.removeQuotes(myElement.getText());
        List<InertiaPage> pages = locateReferences(true);

        for (InertiaPage page : pages) {
            if (page.getPath().equals(text)) {
                return page.getFile();
            }
        }

        return null;
    }

    @Override
    public Object @NotNull [] getVariants() {
        List<InertiaPage> pages = locateReferences(false);

        List<LookupElementBuilder> variants = new ArrayList<>();
        for (InertiaPage page : pages) {
            variants.add(
                PsiUtil.buildSimpleLookupElement(page.getPath())
            );
        }

        return variants.toArray();
    }

    public List<InertiaPage> locateReferences(boolean withFile) {
        List<InertiaPage> references = new ArrayList<>();

        String[] paths = SettingsState.getInstance(project).getInertiaPageComponentRootPath().split(";");

        List<PsiDirectory> dirs = new ArrayList<>();
        for (String path : paths) {
            PsiDirectory potentialDir = DirectoryPsiUtil.getDirectory(
                project,
                StrUtil.removeDoubleSlashes(
                    StrUtil.addSlashes(
                        path.replace("\\", "/")
                    )
                )
            );

            if (potentialDir != null) {
                dirs.add(potentialDir);
            }
        }

        if (dirs.isEmpty()) {
            return references;
        }

        for (PsiDirectory dir : dirs) {
            for (PsiDirectory subDir : dir.getSubdirectories()) {
                references.addAll(getPages(subDir, withFile));
            }
        }

        return references;
    }

    public List<InertiaPage> getPages(PsiDirectory dir, boolean withFile) {

        List<InertiaPage> pages = new ArrayList<>(processFilesInDirectory(dir, dir.getFiles(), withFile, ""));

        for (PsiDirectory subDir : dir.getSubdirectories()) {
            pages.addAll(processFilesInDirectory(subDir, subDir.getFiles(), withFile, dir.getName()));
        }

        return pages;
    }

    private List<InertiaPage> processFilesInDirectory(PsiDirectory dir, PsiFile[] files, boolean withFile, String parentPath) {
        List<InertiaPage> pages = new ArrayList<>();

        for (PsiFile psiFile : files) {

            String fileName = psiFile.getName();

            if (fileName.endsWith(".vue") || fileName.endsWith(".jsx")) {

                String pageName = parentPath.isEmpty() ? dir.getName() : parentPath + "/" + dir.getName();
                pageName = pageName + "/" + fileName.replaceFirst("\\.(vue|jsx)$", "");

                if (withFile) {
                    pages.add(new InertiaPage(pageName, psiFile));
                } else {
                    pages.add(new InertiaPage(pageName));
                }
            }
        }

        return pages;
    }
}

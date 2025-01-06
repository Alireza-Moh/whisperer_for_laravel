package at.alirezamoh.whisperer_for_laravel.blade.component;

import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.blade.BladeLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides code completion for Laravel Blade component tags (e.g., <x-...>).
 * This contributor suggests known Blade components as the user types.
 */
public class BladeXComponentCompletionContributor extends CompletionContributor {
    BladeXComponentCompletionContributor() {
        PsiElementPattern.Capture<PsiElement> pattern =
            PlatformPatterns.psiElement(PsiElement.class)
                .inVirtualFile(PlatformPatterns.virtualFile().ofType(BladeFileType.INSTANCE))
                .andNot(PlatformPatterns.psiElement(PsiElement.class).withLanguage(BladeLanguage.INSTANCE))
                .withText(StandardPatterns.string().startsWith("x-"));
        extend(
            CompletionType.BASIC,
            pattern,
            new CompletionProvider<>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                    PsiElement element = completionParameters.getPosition();
                    Project project = element.getProject();

                    if (!PluginUtils.isLaravelProject(project) && PluginUtils.isLaravelFrameworkNotInstalled(project)) {
                        return;
                    }

                    completionResultSet.addAllElements(collectBladeComponents(project));
                }
            }
        );
    }

    /**
     *Collects Blade component pages
     *
     * @param project  The current project
     * @return         A list of all blade components
     */
    private List<LookupElementBuilder> collectBladeComponents(Project project) {
        List<LookupElementBuilder> variants = new ArrayList<>();
        Collection<VirtualFile> bladeFiles = FileTypeIndex.getFiles(BladeFileType.INSTANCE, GlobalSearchScope.projectScope(project));
        PsiManager psiManager = PsiManager.getInstance(project);

        for (VirtualFile file : bladeFiles) {
            PsiFile psiFile = psiManager.findFile(file);
            if (psiFile != null) {
                String fullPath = psiFile.getVirtualFile().getPath().replace("\\", "/");

                int idx = fullPath.indexOf("views/components/");
                if (idx != -1) {
                    String relativePath = fullPath.substring(idx + "views/components/".length());

                    relativePath = relativePath.replaceFirst("\\.blade\\.php$", "");

                    String componentName = relativePath.replace('/', '.');

                    variants.add(
                        PsiElementUtils.buildSimpleLookupElement("x-" + componentName)
                    );
                }
            }
        }

        return variants;
    }
}

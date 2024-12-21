package at.alirezamoh.whisperer_for_laravel.blade.viewName;

import at.alirezamoh.whisperer_for_laravel.blade.viewName.visitors.BladeFileCollector;
import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Provides references to blade files within a Laravel project
 */
public class BladeReference extends PsiReferenceBase<PsiElement> {
    /**
     * The current project
     */
    private Project project;

    /**
     * Collects all blade files
     */
    private BladeFileCollector bladeFileCollector;

    /**
     * @param element        The PSI element being referenced
     * @param rangeInElement The text range of the reference within the element
     */
    public BladeReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);

        this.project = element.getProject();
        this.bladeFileCollector = new BladeFileCollector(project);
    }

    /**
     * Resolves the reference to the corresponding blade file
     * @return The resolved PSI element (Blade file) or null if not found
     */
    @Override
    public @Nullable PsiElement resolve() {
        String bladeReference = StrUtil.removeQuotes(myElement.getText());

        bladeFileCollector.setWithPsiFile(true);
        Map<PsiFile, String> bladeFilesWithCorrectPsiFile = bladeFileCollector.startSearching()
            .getBladeFilesWithCorrectPsiFile();

        for (Map.Entry<PsiFile, String> entry : bladeFilesWithCorrectPsiFile.entrySet()) {
            if (entry.getValue().equals(bladeReference)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Returns an array of variants (code completion suggestions) for the reference
     * @return An array of LookupElementBuilder
     */
    @Override
    public Object @NotNull [] getVariants() {
        bladeFileCollector.setWithPsiFile(false);

        return bladeFileCollector.startSearching().getVariants().toArray();
    }
}

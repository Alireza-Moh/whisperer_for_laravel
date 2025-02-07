package at.alirezamoh.whisperer_for_laravel.packages.inertia;

import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
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
        String text = StrUtils.removeQuotes(myElement.getText());
        List<InertiaPage> pages = InertiaPageCollector.collectPages(project, true);

        for (InertiaPage page : pages) {
            if (page.getPath().equals(text)) {
                return page.getFile();
            }
        }

        return null;
    }

    @Override
    public Object @NotNull [] getVariants() {
        List<InertiaPage> pages = InertiaPageCollector.collectPages(project, false);

        List<LookupElementBuilder> variants = new ArrayList<>();
        for (InertiaPage page : pages) {
            variants.add(
                PsiElementUtils.buildSimpleLookupElement(page.getPath())
            );
        }

        return variants.toArray();
    }
}
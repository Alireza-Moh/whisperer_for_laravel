package at.alirezamoh.whisperer_for_laravel.packages.livewire.validation;

import at.alirezamoh.whisperer_for_laravel.packages.livewire.property.utils.LivewirePropertyProvider;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class resolves references to livewire component property and provides code completion
 */
public class LivewirePropertyInValidationReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference{
    /**
     * The current project
     */
    private Project project;

    /**
     * @param element        The PSI element representing the property
     * @param rangeInElement The text range of the reference within the element
     */
    public LivewirePropertyInValidationReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);

        this.project = element.getProject();
    }

    @Override
    public @Nullable PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    /**
     * Returns an array of route names
     * @return route names list
     */
    @Override
    public Object @NotNull [] getVariants() {
        List<LookupElementBuilder> variants = LivewirePropertyProvider.collectProperties(
            project,
            myElement.getContainingFile(),
            true
        );

        if (variants == null) {
            return new Object[0];
        }

        return variants.toArray();
    }

    /**
     * Resolves the route name reference to the corresponding PSI element
     * This method searches for the property in the component
     * @return The resolved property or null
     */
    @Override
    public ResolveResult @NotNull [] multiResolve(boolean b) {
        List<PsiElement> resolvedProperties = LivewirePropertyProvider.resolveProperty(
            project,
            myElement.getContainingFile(),
            StrUtils.removeQuotes(myElement.getText()),
            true
        );

        if (resolvedProperties == null) {
            return ResolveResult.EMPTY_ARRAY;
        }

        List<ResolveResult> results = new ArrayList<>();
        for (PsiElement property : resolvedProperties) {
            results.add(new PsiElementResolveResult(property));
        }

        return results.toArray(new ResolveResult[0]);
    }
}

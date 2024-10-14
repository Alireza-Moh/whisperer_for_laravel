package at.alirezamoh.idea_whisperer_for_laravel.blade;


import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

/**
 * Contributes Blade file references to the PSI tree
 */
public class BladeReferenceContributor extends PsiReferenceContributor {
    /**
     * The name of the 'view' function
     */
    private final String VIEW_FUNCTION = "view";

    /**
     * The namespace of the 'View' facade
     */
    private final String VIEW_NAMESPACE = "\\Illuminate\\Support\\Facades\\View";

    /**
     * The names of the methods in the 'View' facade that can reference Blade files
     */
    private final String[] VIEW_METHODS = {"make", "first", "exists", "composer", "creator"} ;

    /**
     * The namespace of the 'Route' facade
     */
    private final String ROUTE_NAMESPACE = "\\Illuminate\\Support\\Facades\\Route";

    /**
     * The names of the methods in the 'Route' facade that can reference blade files
     */
    private final String[] ROUTE_METHOD_NAMES = {"view"};

    /**
     * Registers the reference provider for Blade files
     * @param psiReferenceRegistrar The PSI reference registrar
     */
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
                PlatformPatterns.psiElement(StringLiteralExpression.class),
                new PsiReferenceProvider() {

                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
                        if (isInsideViewMethods(psiElement) && psiElement instanceof StringLiteralExpression stringLiteralExpression) {
                            String text = stringLiteralExpression.getText();

                            return new PsiReference[]{new BladeReference(
                                psiElement,
                                new TextRange(PsiUtil.getStartOffset(text), PsiUtil.getEndOffset(text))
                            )};
                        }
                        return PsiReference.EMPTY_ARRAY;
                    }
                }
        );
    }

    /**
     * Checks if the given PSI element is inside a method or function that can reference Blade files
     * @param psiElement The PSI element to check
     * @return True if the element is inside a relevant method or function, false otherwise
     */
    private boolean isInsideViewMethods(@NotNull PsiElement psiElement) {
        return PsiUtil.isInsideFunction(psiElement, VIEW_FUNCTION)
            || PsiUtil.isInsideMethod(psiElement, VIEW_METHODS, VIEW_NAMESPACE)
            || PsiUtil.isInsideMethod(psiElement, ROUTE_METHOD_NAMES, ROUTE_NAMESPACE);
    }
}

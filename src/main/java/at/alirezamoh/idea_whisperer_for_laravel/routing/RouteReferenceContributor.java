package at.alirezamoh.idea_whisperer_for_laravel.routing;

import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

/**
 * Contributes references to Laravel routes in the PSI tree
 * This class allows the IDE to recognize and resolve route names
 * used in the `route` and `to_route` helper functions
 */
public class RouteReferenceContributor extends PsiReferenceContributor {
    /**
     * The names of the route helper functions
     */
    private final String[] ROUTE_METHODS = {"route", "to_route"} ;

    /**
     * Registers the reference provider for route names
     * @param psiReferenceRegistrar The PSI reference registrar
     */
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression.class),
            new PsiReferenceProvider() {
                @Override
                public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
                    if(isInsideRouteHelperMethod(psiElement) && psiElement instanceof StringLiteralExpression stringLiteralExpression)
                    {
                        String text = stringLiteralExpression.getText();

                        return new PsiReference[]{
                            new RouteReference(psiElement, new TextRange(PsiUtil.getStartOffset(text), PsiUtil.getEndOffset(text)))
                        };
                    }
                    return PsiReference.EMPTY_ARRAY;
                }
            }
        );
    }

    /**
     * Checks if the given PSI element is inside a `route` or `to_route` helper function call
     * @param psiElement The PSI element to check
     * @return           True if the element is inside a route helper function call, false otherwise
     */
    private boolean isInsideRouteHelperMethod(@NotNull PsiElement psiElement) {
        return PsiUtil.isInsideFunction(psiElement, ROUTE_METHODS[0])
            || PsiUtil.isInsideFunction(psiElement, ROUTE_METHODS[1]);
    }
}

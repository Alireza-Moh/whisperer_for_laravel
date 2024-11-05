package at.alirezamoh.idea_whisperer_for_laravel.routing;

import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.MethodUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class allows the IDE to recognize and resolve route names
 */
public class RouteReferenceContributor extends PsiReferenceContributor {
    /**
     * The names of the route helper functions
     */
    public static Map<String, List<Integer>> ROUTE_METHODS = new HashMap<>() {{
        put("route", List.of(0));
        put("to_route", List.of(0));
    }};

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
                    if (FrameworkUtils.isLaravelFrameworkNotInstalled(psiElement.getProject())) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    if(psiElement instanceof StringLiteralExpression stringLiteralExpression && isInsideRouteFunctions(psiElement))
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
     * @return           True or false
     */
    private boolean isInsideRouteFunctions(@NotNull PsiElement psiElement) {
        FunctionReference function = MethodUtils.resolveFunctionReference(psiElement, 10);

        return function != null && isViewParam(function, psiElement);
    }

    private boolean isViewParam(FunctionReference reference, PsiElement position) {
        int paramIndex = MethodUtils.findParamIndex(position, false);

        List<Integer> paramPositions = ROUTE_METHODS.get(reference.getName());

        return paramPositions != null && paramPositions.contains(paramIndex);
    }
}

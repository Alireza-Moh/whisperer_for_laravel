package at.alirezamoh.whisperer_for_laravel.routing;

import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
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
    private final Map<String, Integer> ROUTE_METHODS = new HashMap<>() {{
        put("route", 0);
        put("to_route", 0);
        put("signedRoute", 0);
    }};

    /**
     * Classes to provide route names autocompletion
     */
    private final List<String> ROUTE_CLASSES = List.of("\\Illuminate\\Support\\Facades\\Redirect", "\\Illuminate\\Support\\Facades\\URL");

    /**
     * Redirect and URL class methods
     */
    private final Map<String, Integer> REDIRECT_AND_URL_METHODS = new HashMap<>() {{
        put("route", 0);
        put("signedRoute", 0);
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
                    Project project = psiElement.getProject();

                    if (!FrameworkUtils.isLaravelProject(project) && FrameworkUtils.isLaravelFrameworkNotInstalled(project)) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    if(isInsideCorrectMethod(psiElement))
                    {
                        String text = psiElement.getOriginalElement().getText();

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
    private boolean isInsideCorrectMethod(@NotNull PsiElement psiElement) {
        MethodReference method = MethodUtils.resolveMethodReference(psiElement, 10);
        FunctionReference function = MethodUtils.resolveFunctionReference(psiElement, 10);
        Project project = psiElement.getProject();

        return (
            method != null
                && isRouteParam(method, psiElement)
                && ClassUtils.isCorrectRelatedClass(method, project, ROUTE_CLASSES)
            ) 
            || (function != null && isRouteParam(function, psiElement));
    }

    /**
     * General method to check if the given reference and position match the config parameter criteria
     * @param reference The method or function reference
     * @param position The PSI element position
     * @return True or false
     */
    private boolean isRouteParam(PsiElement reference, PsiElement position) {
        String referenceName = (reference instanceof MethodReference)
            ? ((MethodReference) reference).getName()
            : ((FunctionReference) reference).getName();

        Integer expectedParamIndex = REDIRECT_AND_URL_METHODS.get(referenceName);

        if (expectedParamIndex == null) {
            expectedParamIndex = ROUTE_METHODS.get(referenceName);
        }

        if (expectedParamIndex == null) {
            return false;
        }

        return MethodUtils.findParamIndex(position, false) == expectedParamIndex;
    }
}

package at.alirezamoh.whisperer_for_laravel.routing.routeAction;

import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * This class allows the IDE to recognize and resolve controllers with its methods
 */
public class RouteActionReferenceContributor extends PsiReferenceContributor {
    /**
     * The names of the route methods
     */
    public static Map<String, Integer> ROUTE_METHODS = new HashMap<>() {{
        put("get", 1);
        put("post", 1);
        put("put", 1);
        put("delete", 1);
        put("patch", 1);
        put("options", 1);
        put("any", 1);
        put("match", 1);
        put("fallback", 0);
    }};

    /**
     * Route class
     */
    private final String[] ROUTE_NAMESPACES = {"\\Illuminate\\Routing\\Route", "\\Illuminate\\Support\\Facades\\Route", "\\Route"};

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.or(
                PlatformPatterns.psiElement(StringLiteralExpression.class).withParent(ParameterList.class),
                PlatformPatterns.psiElement(StringLiteralExpression.class).withSuperParent(3, ParameterList.class)
            ),
            new PsiReferenceProvider() {
                @Override
                public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
                    Project project = psiElement.getProject();

                    if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    if (!(psiElement instanceof StringLiteralExpression stringLiteralExpression)) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    if(isInsideCorrectMethod(psiElement))
                    {
                        return new PsiReference[]{
                            new RouteActionReference(
                                stringLiteralExpression,
                                new TextRange(
                                    PsiElementUtils.getStartOffset(stringLiteralExpression),
                                    PsiElementUtils.getEndOffset(stringLiteralExpression)
                                )
                            )
                        };
                    }
                    return PsiReference.EMPTY_ARRAY;
                }
            }
        );
    }

    /**
     * Checks if the given PSI element is inside one of the route methods
     * @param psiElement The PSI element to check
     * @return           True or false
     */
    private boolean isInsideCorrectMethod(@NotNull PsiElement psiElement) {
        MethodReference method = MethodUtils.resolveMethodReference(psiElement, 10);
        Project project = psiElement.getProject();

        return method != null
            && isRouteActionParam(method, psiElement)
            && PhpClassUtils.isCorrectRelatedClass(method, project, ROUTE_NAMESPACES);
    }

    /**
     * Check if the given method and position match the action parameter criteria
     * @param methodReference The method
     * @param position The PSI element position
     * @return True or false
     */
    private boolean isRouteActionParam(MethodReference methodReference, PsiElement position) {
        String methodName = methodReference.getName();
        if (methodName == null) {
            return false;
        }

        Integer expectedParamIndex = ROUTE_METHODS.get(methodName);
        if (expectedParamIndex == null) {
            return false;
        }

        return MethodUtils.findParamIndex(position, false) == expectedParamIndex;
    }
}

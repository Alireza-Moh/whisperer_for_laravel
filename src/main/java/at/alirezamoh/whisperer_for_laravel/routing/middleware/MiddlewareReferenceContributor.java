package at.alirezamoh.whisperer_for_laravel.routing.middleware;

import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class allows the IDE to recognize and resolve middleware aliases and groups
 */
public class MiddlewareReferenceContributor extends PsiReferenceContributor {
    /**
     * The names of the middleware methods
     */
    private final Map<String, Integer> MIDDLEWARE_METHODS = new HashMap<>() {{
        put("middleware", 0);
        put("withoutMiddleware", 0);
    }};

    /**
     * Route class
     */
    private final List<String> ROUTE_NAMESPACES = new ArrayList<>() {{
        add("\\Illuminate\\Routing\\Route");
        add("\\Illuminate\\Support\\Facades\\Route");
        add("\\Route");
    }};

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

                    if (!FrameworkUtils.isLaravelProject(project) && FrameworkUtils.isLaravelFrameworkNotInstalled(project)) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    if(isInsideCorrectMethod(psiElement))
                    {
                        String text = psiElement.getOriginalElement().getText();

                        return new PsiReference[]{
                            new MiddlewareReference(psiElement, new TextRange(PsiUtil.getStartOffset(text), PsiUtil.getEndOffset(text)))
                        };
                    }
                    return PsiReference.EMPTY_ARRAY;
                }
            }
        );
    }

    /**
     * Checks if the given PSI element is inside one of the middleware methods
     * @param psiElement The PSI element to check
     * @return           True or false
     */
    private boolean isInsideCorrectMethod(@NotNull PsiElement psiElement) {
        MethodReference method = MethodUtils.resolveMethodReference(psiElement, 10);
        Project project = psiElement.getProject();

        return method != null
            && isMiddlewareParam(method, psiElement)
            && ClassUtils.isCorrectRelatedClass(method, project, ROUTE_NAMESPACES);
    }

    /**
     * Check if the given method and position match the middleware parameter criteria
     * @param methodReference The method
     * @param position The PSI element position
     * @return True or false
     */
    private boolean isMiddlewareParam(MethodReference methodReference, PsiElement position) {
        Integer expectedParamIndex = MIDDLEWARE_METHODS.get(methodReference.getName());

        if (expectedParamIndex == null) {
            return false;
        }

        return MethodUtils.findParamIndex(position, false) == expectedParamIndex;
    }
}

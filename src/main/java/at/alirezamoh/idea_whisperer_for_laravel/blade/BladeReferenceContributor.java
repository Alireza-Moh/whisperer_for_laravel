package at.alirezamoh.idea_whisperer_for_laravel.blade;

import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.MethodUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.ArrayCreationExpressionImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpPsiElementImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contributes Blade file references to the PSI tree
 */
public class BladeReferenceContributor extends PsiReferenceContributor {
    /**
     * The names of the methods in the 'View' facade that can reference Blade files
     */
    public static Map<String, Integer> VIEW_METHODS = new HashMap<>() {{
        put("make", 0);
        put("first", 0);
        put("exists", 0);
        put("composer", 0);
        put("creator", 0);
    }};

    /**
     * The names of the methods in the 'Route' facade that can reference Blade files
     */
    public static Map<String, Integer> ROUTE_METHODS = new HashMap<>() {{
        put("view", 0);
    }};

    /**
     * The FQN of the 'Route' facade
     */
    private final String ROUTE = "\\Illuminate\\Support\\Facades\\Route";

    /**
     * The FQN of the 'View' facade
     */
    private final String VIEW = "\\Illuminate\\Support\\Facades\\View";

    /**
     * Registers the reference provider for Blade files
     * @param psiReferenceRegistrar The PSI reference registrar
     */
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.or(
                PlatformPatterns.psiElement(StringLiteralExpression.class).withSuperParent(1, ParameterList.class),
                PlatformPatterns.psiElement(StringLiteralExpression.class).withSuperParent(1, ArrayCreationExpressionImpl.class),
                PlatformPatterns.psiElement(StringLiteralExpression.class).withSuperParent(1, PhpPsiElementImpl.class)
            ),
            new PsiReferenceProvider() {

                @Override
                public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
                    if (FrameworkUtils.isLaravelFrameworkNotInstalled(psiElement.getProject())) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    if (isInsideViewMethods(psiElement)) {
                        String text = psiElement.getText();

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
     * @return True or false
     */
    private boolean isInsideViewMethods(@NotNull PsiElement psiElement) {
        MethodReference method = MethodUtils.resolveMethodReference(psiElement, 10);
        FunctionReference function = MethodUtils.resolveFunctionReference(psiElement, 10);
        Project project = psiElement.getProject();

        return (method != null && isViewMethodParam(method, psiElement) && isViewOrRouteFacadeMethod(method, project))
                || (function != null && isViewFunctionParam(function, psiElement));
    }

    /**
     * General method to check if the given reference and position match the view parameter criteria
     * @param methodReference The method or function reference
     * @param position The PSI element position
     * @return True or false
     */
    private boolean isViewMethodParam(MethodReference methodReference, PsiElement position) {
        Integer expectedParamIndex = VIEW_METHODS.get(methodReference.getName());

        if (expectedParamIndex == null) {
            return false;
        }

        return expectedParamIndex == MethodUtils.findParamIndex(position, false);
    }

    /**
     * General method to check if the given function and position match the view function parameter criteria
     * @param functionReference The function reference
     * @param position The PSI element position
     * @return True or false
     */
    private boolean isViewFunctionParam(FunctionReference functionReference, PsiElement position) {
        return ROUTE_METHODS.containsKey(functionReference.getName())
            && 0 == MethodUtils.findParamIndex(position, false);
    }

    /**
     * Checks if the given method reference is a view or route method
     * @param methodReference The method reference
     * @param project The project context
     * @return True or false
     */
    private boolean isViewOrRouteFacadeMethod(MethodReference methodReference, Project project) {
        String methodName = methodReference.getName();
        List<PhpClassImpl> resolvedClasses = MethodUtils.resolveMethodClasses(methodReference, project);

        PhpClass routeClass = ClassUtils.getClassByFQN(project, ROUTE);
        PhpClass viewClass = ClassUtils.getClassByFQN(project, VIEW);

        return (
            ROUTE_METHODS.containsKey(methodName)
                && routeClass != null
                && resolvedClasses.stream().anyMatch(clazz -> ClassUtils.isChildOf(clazz, routeClass))
            )
            ||
            (
                VIEW_METHODS.containsKey(methodName)
                    && viewClass != null
                    && resolvedClasses.stream().anyMatch(clazz -> ClassUtils.isChildOf(clazz, viewClass))
            );
    }
}

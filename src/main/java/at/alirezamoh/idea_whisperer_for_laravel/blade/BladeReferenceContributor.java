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
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
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
    public static Map<String, List<Integer>> VIEW_METHODS = new HashMap<>() {{
        put("make", List.of(0));
        put("first", List.of(0));
        put("exists", List.of(0));
        put("composer", List.of(0));
        put("creator", List.of(0));
        put("view", List.of(0));
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
                PlatformPatterns.psiElement(StringLiteralExpression.class),
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

        return (method != null && isViewParam(method, psiElement) && isViewOrRouteMethod(method, project))
                || (function != null && isViewParam(function, psiElement));
    }

    /**
     * General method to check if the given reference and position match the view parameter criteria
     * @param reference The method or function reference
     * @param position The PSI element position
     * @return True or false
     */
    private boolean isViewParam(PsiElement reference, PsiElement position) {
        int paramIndex = MethodUtils.findParamIndex(position, false);
        String referenceName = (reference instanceof MethodReference)
                ? ((MethodReference) reference).getName()
                : ((FunctionReference) reference).getName();

        List<Integer> paramPositions = VIEW_METHODS.get(referenceName);

        return paramPositions != null && paramPositions.contains(paramIndex);
    }

    /**
     * Checks if the given method reference is a view or route method
     * @param methodReference The method reference
     * @param project The project context
     * @return True or false
     */
    private boolean isViewOrRouteMethod(MethodReference methodReference, Project project) {
        List<PhpClassImpl> resolvedClasses = MethodUtils.resolveMethodClasses(methodReference, project);

        PhpClass routeClass = ClassUtils.getClassByFQN(project, ROUTE);
        PhpClass viewClass = ClassUtils.getClassByFQN(project, VIEW);

        return (routeClass != null && resolvedClasses.stream().anyMatch(clazz -> ClassUtils.isChildOf(clazz, routeClass)))
                || (viewClass != null && resolvedClasses.stream().anyMatch(clazz -> ClassUtils.isChildOf(clazz, viewClass)));
    }
}

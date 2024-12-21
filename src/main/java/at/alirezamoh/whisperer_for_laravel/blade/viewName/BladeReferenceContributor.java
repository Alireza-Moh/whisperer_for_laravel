package at.alirezamoh.whisperer_for_laravel.blade.viewName;

import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.psiUtil.PsiUtil;
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
        put("view", 1);
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
                    Project project = psiElement.getProject();

                    if (!FrameworkUtils.isLaravelProject(project) && FrameworkUtils.isLaravelFrameworkNotInstalled(project)) {
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
        Project project = psiElement.getProject();

        MethodReference method = MethodUtils.resolveMethodReference(psiElement, 10);
        if (method != null) {
            return isViewMethodParam(method, psiElement) && isViewOrRouteFacadeMethod(method, project);
        }

        FunctionReference function = MethodUtils.resolveFunctionReference(psiElement, 10);
        return function != null && isViewFunctionParam(function, psiElement);
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

        return isExpectedFacadeMethod(methodName, resolvedClasses, routeClass, ROUTE_METHODS)
            || isExpectedFacadeMethod(methodName, resolvedClasses, viewClass, VIEW_METHODS);
    }

    /**
     * Checks if a method matches the expected facade class and method map.
     *
     * @param methodName The method name.
     * @param resolvedClasses The list of resolved classes for the method.
     * @param expectedClass The expected facade class.
     * @param methodMap The map containing method names and expected parameter indices.
     * @return true or false
     */
    private boolean isExpectedFacadeMethod(String methodName, List<PhpClassImpl> resolvedClasses, PhpClass expectedClass, Map<String, Integer> methodMap) {
        return methodMap.containsKey(methodName)
            && expectedClass != null
            && resolvedClasses.stream().anyMatch(clazz -> ClassUtils.isChildOf(clazz, expectedClass));
    }

    /**
     * Checks if a method reference matches a parameter in the 'View' methods.
     *
     * @param methodReference The method reference.
     * @param position The PSI element position.
     * @return true or false
     */
    private boolean isViewMethodParam(MethodReference methodReference, PsiElement position) {
        return isExpectedParam(position, methodReference.getName(), VIEW_METHODS);
    }

    /**
     * Checks if a function reference matches a parameter in the 'Route' methods.
     *
     * @param functionReference The function reference.
     * @param position The PSI element position.
     * @return true or false
     */
    private boolean isViewFunctionParam(FunctionReference functionReference, PsiElement position) {
        return isExpectedParam(position, functionReference.getName(), ROUTE_METHODS);
    }

    /**
     * Generalized logic to check if a parameter matches the expected method and map.
     *
     * @param position The PSI element position.
     * @param methodName The method name.
     * @param methodMap The map containing method names and expected parameter indices.
     * @return true or false
     */
    private boolean isExpectedParam(PsiElement position, String methodName, Map<String, Integer> methodMap) {
        Integer expectedParamIndex = methodMap.get(methodName);
        return expectedParamIndex != null && expectedParamIndex == MethodUtils.findParamIndex(position, false);
    }
}

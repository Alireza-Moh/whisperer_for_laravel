package at.alirezamoh.whisperer_for_laravel.inertia;

import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class InertiaReferenceContributor extends PsiReferenceContributor {
    /**
     * The names of the methods in the 'View' facade that can reference Blade files
     */
    public static Map<String, Integer> INERTIA_METHODS = new HashMap<>() {{
        put("render", 0);
    }};

    /**
     * The names of the methods in the 'View' facade that can reference Blade files
     */
    public static Map<String, Integer> ROUTE_INERTIA_METHODS = new HashMap<>() {{
        put("inertia", 0);
    }};

    /**
     * The FQN of the 'Route' facade
     */
    private final String INERTIA = "\\Inertia\\Inertia";

    /**
     * The FQN of the 'Route' facade
     */
    private final String ROUTE = "\\Illuminate\\Support\\Facades\\Route";

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
            psiElement(StringLiteralExpression.class).withParent(psiElement(ParameterList.class)),
            new PsiReferenceProvider() {

                @Override
                public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
                    Project project = psiElement.getProject();

                    if (!FrameworkUtils.isLaravelProject(project) && FrameworkUtils.isLaravelFrameworkNotInstalled(project)) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    if (isInsideCorrectMethod(psiElement)) {
                        String text = psiElement.getText();

                        return new PsiReference[]{new InertiaReference(
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
     * Checks if a given PSI element is inside a valid Inertia method, Route method, or function.
     *
     * @param psiElement The PSI element to check.
     * @return True if the element is inside a valid method or function, false otherwise.
     */
    private boolean isInsideCorrectMethod(@NotNull PsiElement psiElement) {
        MethodReference method = MethodUtils.resolveMethodReference(psiElement, 10);
        Project project = psiElement.getProject();

        if (method != null) {
            return isInertiaMethod(method, psiElement, project) || isRouteMethod(method, psiElement, project);
        }

        return isInertiaFunction(psiElement);
    }

    /**
     * Checks if a given method reference matches an Inertia method.
     *
     * @param methodReference The method reference.
     * @param position        The PSI element position.
     * @param project         The current project.
     * @return                true or false
     */
    private boolean isInertiaMethod(MethodReference methodReference, PsiElement position, Project project) {
        return isExpectedMethod(methodReference, position, project, INERTIA_METHODS, INERTIA);
    }

    /**
     * Checks if a given method reference matches a Route method.
     *
     * @param methodReference The method reference.
     * @param position        The PSI element position.
     * @param project         The current project.
     * @return                true or false
     */
    private boolean isRouteMethod(MethodReference methodReference, PsiElement position, Project project) {
        return isExpectedMethod(methodReference, position, project, ROUTE_INERTIA_METHODS, ROUTE);
    }

    /**
     * Generalized logic for matching method references to a specific class and parameter criteria.
     *
     * @param methodReference   The method reference.
     * @param position          The PSI element position.
     * @param project           The current project.
     * @param methodMap         Map of method names and their parameter indices.
     * @param expectedClassFQN  Fully qualified name of the expected class.
     * @return                  true or false
     */
    private boolean isExpectedMethod(
        MethodReference methodReference,
        PsiElement position,
        Project project,
        Map<String, Integer> methodMap,
        String expectedClassFQN
    ) {
        Integer expectedParamIndex = methodMap.get(methodReference.getName());

        if (expectedParamIndex == null) {
            return false;
        }

        List<PhpClassImpl> resolvedClasses = getPhpClassesForMethod(methodReference, project);
        PhpClass expectedClass = ClassUtils.getClassByFQN(project, expectedClassFQN);

        return expectedClass != null
            && resolvedClasses.stream().anyMatch(clazz -> ClassUtils.isChildOf(clazz, expectedClass))
            && expectedParamIndex == MethodUtils.findParamIndex(position, false);
    }

    private @NotNull List<PhpClassImpl> getPhpClassesForMethod(MethodReference methodReference, Project project) {
        return MethodUtils.resolveMethodClasses(methodReference, project);
    }

    /**
     * Checks if the given method reference is a view or route method
     * @param position psi element
     * @return         true or false
     */
    private boolean isInertiaFunction(PsiElement position) {
        FunctionReference functionReference = MethodUtils.resolveFunctionReference(position, 10);
        if (functionReference == null) {
            return false;
        }

        Integer expectedParamIndex = ROUTE_INERTIA_METHODS.get(functionReference.getName());

        if (expectedParamIndex == null) {
            return false;
        }

        return expectedParamIndex == MethodUtils.findParamIndex(position, false);
    }
}

package at.alirezamoh.whisperer_for_laravel.packages.inertia;

import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InertiaMethodValidator {
    /**
     * The names of the methods in the 'View' facade that can reference Blade files
     */
    private static final Map<String, Integer> INERTIA_METHODS = new HashMap<>() {{
        put("render", 0);
    }};

    /**
     * The names of the methods in the 'View' facade that can reference Blade files
     */
    private static final Map<String, Integer> ROUTE_INERTIA_METHODS = new HashMap<>() {{
        put("inertia", 0);
    }};

    /**
     * The FQN of the 'Route' facade
     */
    private static final String INERTIA = "\\Inertia\\Inertia";

    /**
     * The FQN of the 'Route' facade
     */
    private static final String ROUTE = "\\Illuminate\\Support\\Facades\\Route";

    private InertiaMethodValidator() {}

    /**
     * Checks if a given PSI element is inside a valid Inertia method, Route method, or function.
     *
     * @param psiElement The PSI element to check.
     * @return True if the element is inside a valid method or function, false otherwise.
     */
    public static boolean isInsideCorrectMethod(@NotNull PsiElement psiElement) {
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
    private static boolean isInertiaMethod(MethodReference methodReference, PsiElement position, Project project) {
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
    private static boolean isRouteMethod(MethodReference methodReference, PsiElement position, Project project) {
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
    private static boolean isExpectedMethod(
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
        PhpClass expectedClass = PhpClassUtils.getClassByFQN(project, expectedClassFQN);

        return expectedClass != null
            && resolvedClasses.stream().anyMatch(clazz -> PhpClassUtils.isChildOf(clazz, expectedClass))
            && expectedParamIndex == MethodUtils.findParamIndex(position, false);
    }

    /**
     * Checks if the given method reference is a view or route method
     * @param position psi element
     * @return         true or false
     */
    private static boolean isInertiaFunction(PsiElement position) {
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

    private static @NotNull List<PhpClassImpl> getPhpClassesForMethod(MethodReference methodReference, Project project) {
        return MethodUtils.resolveMethodClasses(methodReference, project);
    }
}

package at.alirezamoh.whisperer_for_laravel.routing;

import at.alirezamoh.whisperer_for_laravel.indexes.RouteIndex;
import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.IdFilter;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RouteUtils {
    /**
     * The names of the route helper functions
     */
    public static final Map<String, Integer> ROUTE_METHODS = new HashMap<>() {{
        put("get", 1);
        put("post", 1);
        put("put", 1);
        put("delete", 1);
        put("patch", 1);
        put("options", 1);
        put("any", 1);
        put("fallback", 0);
        put("match", 0);
    }};

    /**
     * The namespaces of the `Route` facade and class
     */
    public static final List<String> ROUTE_NAMESPACES = new ArrayList<>() {{
        add("\\Illuminate\\Routing\\Route");
        add("\\Illuminate\\Support\\Facades\\Route");
        add("\\Route");
    }};

    /**
     * The names of the route helper functions
     */
    private final static Map<String, Integer> ROUTE_NAME_METHODS = new HashMap<>() {{
        put("route", 0);
        put("to_route", 0);
        put("signedRoute", 0);
    }};

    /**
     * Classes to provide route names autocompletion
     */
    private final static String[] ROUTE_CLASSES = {"\\Illuminate\\Support\\Facades\\Redirect", "\\Illuminate\\Support\\Facades\\URL"};

    /**
     * Redirect and URL class methods
     */
    private final static Map<String, Integer> REDIRECT_AND_URL_METHODS = new HashMap<>() {{
        put("route", 0);
        put("signedRoute", 0);
    }};

    /**
     * Checks if the given method reference is a Laravel route method
     *
     * @param methodReference the method reference to check
     * @return true or false
     */
    public static boolean isLaravelRouteMethod(MethodReference methodReference) {
        ClassReferenceImpl routeClassReference = PhpClassUtils.getClassReferenceImplFromMethodRef(methodReference);

        return routeClassReference != null
            && ROUTE_METHODS.containsKey(methodReference.getName())
            && routeClassReference.getFQN() != null
            && ROUTE_NAMESPACES.contains(routeClassReference.getFQN());
    }

    public static String[] getRouteNamespacesAsArray(String ...additionalNamespaces) {
        if (additionalNamespaces != null) {
            for (String namespace : additionalNamespaces) {
                if (!ROUTE_NAMESPACES.contains(namespace)) {
                    ROUTE_NAMESPACES.add(namespace);
                }
            }
        }

        return ROUTE_NAMESPACES.toArray(new String[0]);
    }

    /**
     * Checks if the given PSI element is inside a `route` or `to_route` helper function call
     * @param psiElement The PSI element to check
     * @return           True or false
     */
    public static boolean isInsideCorrectRouteNameMethod(@NotNull PsiElement psiElement) {
        MethodReference method = MethodUtils.resolveMethodReference(psiElement, 10);
        FunctionReference function = MethodUtils.resolveFunctionReference(psiElement, 10);
        Project project = psiElement.getProject();

        return (
            method != null
                && isRouteParam(method, psiElement)
                && PhpClassUtils.isCorrectRelatedClass(method, project, ROUTE_CLASSES)
        )
            || (function != null && isRouteParam(function, psiElement));
    }


    /**
     * Retrieves all route names that match the given route name from the index
     *
     * @param routeName The route name to search for
     * @param project   The current project
     * @return A set of matching route names
     */
    public static Set<String> getMatchingRouteNames(String routeName, Project project) {
        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
        Set<String> matchingRouteNames = new HashSet<>();

        fileBasedIndex.processAllKeys(RouteIndex.INDEX_ID, key -> {
            int firstPipeIndex = key.indexOf(" | ");
            if (firstPipeIndex != -1) {
                int secondPipeIndex = key.indexOf(" | ", firstPipeIndex + 3);
                if (secondPipeIndex != -1) {
                    String keyRouteName = key.substring(firstPipeIndex + 3, secondPipeIndex);
                    if (keyRouteName.equals(routeName)) {
                        matchingRouteNames.add(key);
                    }
                }
            }
            return true;
        }, GlobalSearchScope.projectScope(project), IdFilter.getProjectIdFilter(project, false));

        return matchingRouteNames;
    }

    /**
     * Check if the given reference and position match the route name parameter criteria
     * @param reference The method or function reference
     * @param position The PSI element position
     * @return True or false
     */
    private static boolean isRouteParam(PsiElement reference, PsiElement position) {
        String referenceName = (reference instanceof MethodReference)
            ? ((MethodReference) reference).getName()
            : ((FunctionReference) reference).getName();

        if (referenceName == null) {
            return false;
        }

        Integer expectedParamIndex = REDIRECT_AND_URL_METHODS.get(referenceName);
        if (expectedParamIndex == null) {
            expectedParamIndex = ROUTE_NAME_METHODS.get(referenceName);
        }

        if (expectedParamIndex == null) {
            return false;
        }

        return MethodUtils.findParamIndex(position, false) == expectedParamIndex;
    }
}

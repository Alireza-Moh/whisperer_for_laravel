package at.alirezamoh.whisperer_for_laravel.routing;

import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}

package at.alirezamoh.idea_whisperer_for_laravel.routing;

import at.alirezamoh.idea_whisperer_for_laravel.support.IdeaWhispererForLaravelIcon;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.ClassUtils;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This class provides line marker functionality for Laravel routes.
 * It adds gutter icons for methods in a PHP class that are used in routes
 */
public class RouteLineMarker extends RelatedItemLineMarkerProvider {
    /**
     * The names of the route helper functions
     */
    public static Map<String, Integer> ROUTE_METHODS = new HashMap<>() {{
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
    private final List<String> ROUTE_NAMESPACES = new ArrayList<>() {{
        add("\\Illuminate\\Routing\\Route");
        add("\\Illuminate\\Support\\Facades\\Route");
        add("\\Route");
    }};

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        Project project = element.getProject();
        if (element instanceof PhpClass phpClass) {
            for (Method method : phpClass.getOwnMethods()) {
                if (method.getAccess().isPublic() && !method.isAbstract()) {
                    Collection<PsiReference> references = ReferencesSearch.search(method).findAll();


                    if (!references.isEmpty()) {
                        RelatedItemLineMarkerInfo<PsiElement> relatedItemLineMarkerInfo = createLineMarkerInfo(method, references, project);

                        if (relatedItemLineMarkerInfo != null) {
                            result.add(relatedItemLineMarkerInfo);
                        }
                    }
                }
            }
        }
        super.collectNavigationMarkers(element, result);
    }

    /**
     * Creates a line marker info that represents a method reference and its target declarations
     *
     * @param element    The PSI element representing the method to create a line marker for
     * @param references The references to the method in the code
     * @return A RelatedItemLineMarkerInfo containing the line marker data or null if no valid data is found
     */
    private @Nullable RelatedItemLineMarkerInfo<PsiElement> createLineMarkerInfo(PsiElement element, Collection<PsiReference> references, Project project) {
        PsiElement leafElement = this.findLeafElement(element);

        if (leafElement != null) {
            NavigationGutterIconBuilder<PsiElement> builder =
                NavigationGutterIconBuilder.create(IdeaWhispererForLaravelIcon.LARAVEL_ICON)
                    .setTargets(getDeclarations(references, project))
                    .setPopupTitle("Usage List")
                    .setTooltipText("Navigate to the route declaration");

            return builder.createLineMarkerInfo(leafElement);
        }

        return null;
    }

    /**
     * Retrieves the target declarations from a collection of method references
     * It finds the parent MethodReferenceImpl elements for each reference
     *
     * @param references The collection of method references to process
     * @return A collection of target PSI elements that represent the method references
     */
    private @NotNull Collection<PsiElement> getDeclarations(Collection<PsiReference> references, Project project) {
        return references.stream()
            .map(ref -> {
                PsiElement myElement = ref.getElement();

                while (myElement != null && !(myElement instanceof MethodReferenceImpl)) {
                    myElement = myElement.getParent();
                }

                if (myElement instanceof MethodReferenceImpl methodReference && isInsideCorrectMethod(methodReference, project)) {
                    return myElement;
                }

                return null;
            })
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Finds the leaf element of the provided method since the LineMarker wants a LeafElement
     *
     * @param element The PSI element to find the leaf element for
     * @return The leaf PSI element, or null if no leaf element is found
     */
    private @Nullable PsiElement findLeafElement(PsiElement element) {
        PsiElement current = element;
        while (current != null && current.getFirstChild() != null) {
            current = current.getFirstChild();
        }
        return current;
    }

    /**
     * Checks if the given MethodReferenceImpl is inside a valid method
     * and belongs to a Route class that matches the correct namespaces
     *
     * @param methodReference The MethodReferenceImpl to check
     * @param project The project context to validate the class against the namespaces
     * @return true or false
     */
    private boolean isInsideCorrectMethod(MethodReferenceImpl methodReference, Project project) {
        return methodReference != null
            && ClassUtils.isCorrectRelatedClass(methodReference, project, ROUTE_NAMESPACES)
            && isRouteMethod(methodReference);
    }

    /**
     * Checks if the given MethodReference corresponds to a valid route method
     * This method checks whether the method name is contained in the predefined set of route method names
     *
     * @param methodReference The MethodReference to check
     * @return true or false
     */
    private boolean isRouteMethod(MethodReference methodReference) {
        String methodName = methodReference.getName();

        if (methodName == null) {
            return false;
        }

        return ROUTE_METHODS.containsKey(methodName);
    }
}

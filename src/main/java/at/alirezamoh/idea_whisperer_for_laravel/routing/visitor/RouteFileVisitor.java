package at.alirezamoh.idea_whisperer_for_laravel.routing.visitor;

import at.alirezamoh.idea_whisperer_for_laravel.support.IdeaWhispererForLaravelIcon;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Visits a laravel route file to collect route names and their corresponding URIs.
 * This visitor analyzes the PSI tree of a PHP file and extracts route names
 * defined using the `name` method on the `Route` facade or class
 */

public class RouteFileVisitor extends PsiRecursiveElementWalkingVisitor {
    /**
     * The namespaces of the `Route` facade and class
     */
    private final String[] ROUTE_NAMESPACES = {
        "\\Illuminate\\Routing\\Route",
        "\\Illuminate\\Support\\Facades\\Route",
        "\\Route"
    };

    /**
     * The name of the method used to define route names
     */
    private final String ROUTE_METHOD_NAME = "name";

    /**
     * List of the collected route names
     */
    private List<LookupElementBuilder> suggestions = new ArrayList<>();

    /**
     * Map of MethodReferenceImpl objects and their corresponding route names
     */
    private Map<MethodReferenceImpl, String> routesWithPsi = new HashMap<>();

    /**
     * Visits an element in the PSI tree
     * This method checks if the element is a MethodReferenceImpl representing a call to the `name`
     * method on the `Route` facade or class. If it is, it extracts the route name and URI and builds
     * a LookupElementBuilder for code completion
     *
     * @param element The PSI element being visited
     */
    @Override
    public void visitElement(@NotNull PsiElement element) {
        if (
            element instanceof MethodReferenceImpl methodReference
            && methodReference.getName() != null
            && methodReference.getName().equals(ROUTE_METHOD_NAME)
        ) {
            PhpExpression routeReference = methodReference.getClassReference();
            if (routeReference instanceof MethodReferenceImpl methodReference1) {
                PhpExpression routeClassReference = methodReference1.getClassReference();
                if (
                    routeClassReference instanceof ClassReferenceImpl classReference
                    && Arrays.asList(ROUTE_NAMESPACES).contains(classReference.getFQN())
                ) {
                    this.buildLookupElement(methodReference);
                }
            }
        }

        super.visitElement(element);
    }

    /**
     * Returns the list the collected route names
     *
     * @return The list of LookupElementBuilder objects
     */
    public List<LookupElementBuilder> getSuggestions() {
        return suggestions;
    }

    /**
     * Returns a map of MethodReferenceImpl objects and their corresponding route names
     * @return The map of MethodReferenceImpl objects and route names
     */
    public Map<MethodReferenceImpl, String> getRoutesWithPsi() {
        return routesWithPsi;
    }

    /**
     * Builds a LookupElementBuilder for a route name
     * @param methodReference The MethodReferenceImpl representing the call to the `name` method
     */
    private void buildLookupElement(MethodReferenceImpl methodReference) {
        PsiElement name = methodReference.getParameter(0);

        if (name != null) {
            String modifiedName = StrUtil.removeQuotes(name.getText());

            this.suggestions.add(
                LookupElementBuilder
                    .create(modifiedName)
                    .bold()
                    .withTypeText(this.getRouteUri(methodReference), true)
                    .withIcon(IdeaWhispererForLaravelIcon.LARAVEL_ICON)
            );

            this.routesWithPsi.put(methodReference, modifiedName);
        }
    }

    /**
     * Retrieves the URI of a route
     * @param methodReference The MethodReferenceImpl representing the call to the `name` method
     * @return The URI of the route
     */
    private @NotNull String getRouteUri(MethodReferenceImpl methodReference) {
        String uri = "";
        PsiElement parentMethod = methodReference.getFirstChild();

        if (
            parentMethod instanceof MethodReferenceImpl methodReference1
            && methodReference1.getName() != null
            && !methodReference1.getName().equals(ROUTE_METHOD_NAME)
        ) {
            PhpExpression routeReference = methodReference1.getClassReference();
            if (
                routeReference instanceof ClassReferenceImpl classReference
                && Arrays.asList(ROUTE_NAMESPACES).contains(classReference.getFQN())
            ) {
                PsiElement uriWithQuotes = methodReference1.getParameter(0);
                if (uriWithQuotes != null) {
                    uri = StrUtil.removeQuotes(uriWithQuotes.getText());
                }
            }
        }
        return uri;
    }
}

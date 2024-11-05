package at.alirezamoh.idea_whisperer_for_laravel.routing;

import at.alirezamoh.idea_whisperer_for_laravel.routing.util.RouteUtil;
import at.alirezamoh.idea_whisperer_for_laravel.routing.visitor.RouteNameFinder;
import at.alirezamoh.idea_whisperer_for_laravel.routing.visitor.RouteNamesCollector;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides references to Laravel routes within a project
 * This class resolves references to route names and provides code completion
 * suggestions for route names in various contexts, such as in the `route`
 * helper function or in Blade templates
 */
public class RouteReference extends PsiReferenceBase<PsiElement> {
    /**
     * The current project
     */
    private Project project;

    /**
     * Collects route names
     */
    private RouteNamesCollector routeNamesCollector;

    /**
     * @param element        The PSI element representing the route name reference
     * @param rangeInElement The text range of the reference within the element
     */
    public RouteReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);

        this.project = element.getProject();
        this.routeNamesCollector = new RouteNamesCollector(this.project);
    }

    /**
     * Resolves the route name reference to the corresponding PSI element
     * This method searches for the route name in route files and, if applicable,
     * in module service providers
     * @return The resolved route or null
     */
    @Override
    public @Nullable PsiElement resolve() {
        RouteNameFinder routeNameFinder = new RouteNameFinder(myElement);

        for (PsiFile routeFile : RouteUtil.getAllRouteFiles(project)) {
            routeFile.accept(routeNameFinder);
            PsiElement foundedRoute = routeNameFinder.getFoundedRoute();

            if (foundedRoute != null) {
                return foundedRoute;
            }
        }

        return null;
    }

    /**
     * Returns an array of route names
     * @return route names list
     */
    @Override
    public Object @NotNull [] getVariants() {
        return routeNamesCollector.startSearching().toArray();
    }
}

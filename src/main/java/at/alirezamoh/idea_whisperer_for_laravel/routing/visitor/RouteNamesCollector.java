package at.alirezamoh.idea_whisperer_for_laravel.routing.visitor;

import at.alirezamoh.idea_whisperer_for_laravel.routing.util.RouteUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;

import java.util.*;

public class RouteNamesCollector {
    /**
     * List of the blade files
     */
    private List<LookupElementBuilder> variants = new ArrayList<>();

    /**
     * The current project
     */
    private Project project;

    /**
     * @param project current project
     */
    public RouteNamesCollector(Project project) {
        this.project = project;
    }

    /**
     * Searches for the route names in the route fils
     * @return List of all founded route names
     */
    public List<LookupElementBuilder> startSearching() {
        RouteFileVisitor routeFileVisitor = new RouteFileVisitor();

        for (PsiFile routeFile : RouteUtil.getAllRouteFiles(project)) {
            if (routeFile instanceof PhpFile phpFile) {
                phpFile.accept(routeFileVisitor);

                variants.addAll(routeFileVisitor.getSuggestions());
            }
        }

        return variants;
    }
}

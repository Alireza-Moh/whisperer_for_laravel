package at.alirezamoh.whisperer_for_laravel.routing.routeName.annotator;

import at.alirezamoh.whisperer_for_laravel.packages.inertia.InertiaUtil;
import at.alirezamoh.whisperer_for_laravel.routing.RouteUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

/**
 * Annotator that checks if a route with the given name exists in the project
 * <p>
 */
public class RouteExistenceAnnotator implements Annotator {
    /**
     * Inspects the given PSI element and adds a warning annotation if the route name does not exist
     *
     * @param psiElement       the PSI element to inspect
     * @param annotationHolder the holder to register annotations
     */
    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        Project project = psiElement.getProject();
        if (InertiaUtil.shouldNotCompleteOrNavigate(project)) {
            return;
        }

        if (!(psiElement instanceof StringLiteralExpression stringLiteralExpression)) {
            return;
        }

        if (!RouteUtils.isInsideCorrectRouteNameMethod(stringLiteralExpression)) {
            return;
        }

        if (!doesRouteNameExists(stringLiteralExpression, project)) {
            annotationHolder.newAnnotation(HighlightSeverity.WARNING, "Route name not found")
                .range(stringLiteralExpression.getTextRange())
                .create();
        }
    }

    /**
     * Checks if the route name exists in the project
     *
     * @param stringLiteralExpression the string literal expression containing the route name
     * @param project                 the current project
     * @return true or false
     */
    private boolean doesRouteNameExists(StringLiteralExpression stringLiteralExpression, Project project) {
        String routeName = StrUtils.removeQuotes(stringLiteralExpression.getText());

        return !RouteUtils.getMatchingRouteNames(routeName, project).isEmpty();
    }
}

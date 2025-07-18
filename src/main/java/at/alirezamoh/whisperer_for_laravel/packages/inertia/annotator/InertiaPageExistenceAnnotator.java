package at.alirezamoh.whisperer_for_laravel.packages.inertia.annotator;

import at.alirezamoh.whisperer_for_laravel.packages.inertia.InertiaMethodValidator;
import at.alirezamoh.whisperer_for_laravel.packages.inertia.InertiaPageCollector;
import at.alirezamoh.whisperer_for_laravel.packages.inertia.InertiaUtil;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

/**
 * Annotator that checks if an Inertia page exists
 * <p>
 * This annotator looks for an inertia page path within a PHP method call to "render"
 * It then checks whether the referenced page (based on its path) exists in the project
 * If the page is not found, it creates a warning annotation and provides a quick fix
 * to create the missing Inertia page
 */
public class InertiaPageExistenceAnnotator implements Annotator {
    /**
     * Inspects the given PSI element and adds a warning annotation if an Inertia page is missing
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

        if (!InertiaMethodValidator.isInsideCorrectMethod(stringLiteralExpression)) {
            return;
        }

        if (!doesPageExists(stringLiteralExpression, project)) {
            annotationHolder.newAnnotation(HighlightSeverity.WARNING, "Inertia page not found")
                .range(stringLiteralExpression.getTextRange())
                .withFix(new CreateInertiaPageIntention(stringLiteralExpression.getText()))
                .create();
        }
    }

    private boolean doesPageExists(StringLiteralExpression stringLiteralExpression, Project project) {
        return InertiaPageCollector.collectPages(project, false)
            .stream()
            .anyMatch(page -> page.getPath().equals(StrUtils.removeQuotes(stringLiteralExpression.getText())));
    }
}

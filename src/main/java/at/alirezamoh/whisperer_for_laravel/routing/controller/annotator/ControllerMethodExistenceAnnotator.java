package at.alirezamoh.whisperer_for_laravel.routing.controller.annotator;

import at.alirezamoh.whisperer_for_laravel.routing.RouteUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.*;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Annotator that checks if a controller method exists
 * and adds a warning if it does not.
 */
public class ControllerMethodExistenceAnnotator implements Annotator {
    /**
     * Inspects the given PSI element and adds a warning annotation if the method is missing
     *
     * @param psiElement       the PSI element to inspect
     * @param annotationHolder the holder to register annotations
     */
    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        Project project = psiElement.getProject();
        if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
            return;
        }

        if (!(psiElement instanceof StringLiteralExpression stringLiteralExpression)) {
            return;
        }

        MethodReference method = MethodUtils.resolveMethodReference(psiElement, 10);
        if (!RouteUtils.isLaravelRouteMethod(method)) {
            return;
        }

        PsiElement parentArray = PsiElementUtils.getNthParent(psiElement, 2);

        if (!(parentArray instanceof ArrayCreationExpression arrayCreationExpression)) {
            return;
        }

        ResolvedController resolvedController = getResolvedController(stringLiteralExpression, arrayCreationExpression);

        if (resolvedController != null) {
            if (resolvedController.method == null) {
                annotationHolder.newAnnotation(HighlightSeverity.WARNING, "Controller method not found")
                    .range(stringLiteralExpression.getTextRange())
                    .withFix(new ControllerMethodIntention(resolvedController.phpClass, stringLiteralExpression.getText()))
                    .create();
            }
        }
    }

    /**
     * Attempts to resolve the controller class and method from the given string and array
     *
     * @param stringLiteralExpression the string literal containing the method name 'login'
     * @param arrayCreationExpression the array creation expression containing
     *                                the controller class reference [AuthenticationController::class, 'login']
     * @return a ResolvedController if found, otherwise null
     */
    private @Nullable ResolvedController getResolvedController(StringLiteralExpression stringLiteralExpression, ArrayCreationExpression arrayCreationExpression) {
        String methodName = StrUtils.removeQuotes(stringLiteralExpression.getText());

        for (PsiElement child : arrayCreationExpression.getChildren()) {
            if (!(child instanceof PhpPsiElement phpPsiElement)) {
                return null;
            }

            for (PsiElement subElement : phpPsiElement.getChildren()) {
                PhpClass phpClass = PhpClassUtils.getCachedPhpClassFromClassConstant(subElement);

                if (phpClass != null) {
                    return new ResolvedController(
                        phpClass,
                        phpClass.findOwnMethodByName(methodName),
                        methodName
                    );
                }
            }
        }

        return null;
    }

    record ResolvedController(@NotNull PhpClass phpClass, @Nullable Method method, @NotNull String methodName) {}
}

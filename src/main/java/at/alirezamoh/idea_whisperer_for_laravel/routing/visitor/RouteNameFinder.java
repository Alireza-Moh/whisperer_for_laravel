package at.alirezamoh.idea_whisperer_for_laravel.routing.visitor;

import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Visits a laravel route file to find a route with a specific name
 * This visitor analyzes the PSI tree of a PHP file and searches for a route
 * definition that matches a given route name
 */
public class RouteNameFinder extends PsiRecursiveElementWalkingVisitor {
    /**
     * The name of the method used to define route names
     */
    private final String ROUTE_METHOD_NAME = "name";

    /**
     * The PSI element representing the route name to search for
     */
    private final PsiElement myElement;

    /**
     * The found route element
     */
    private PsiElement foundedRoute;

    /**
     * @param myElement The PSI element representing the route name to search for
     */
    public RouteNameFinder(PsiElement myElement) {
        this.myElement = myElement;
    }

    /**
     * Visits an element in the route file
     * This method checks if the element is a MethodReferenceImpl representing a call to the `name`
     * method. If it is, it checks if the route name parameter matches the target route name
     * @param element The PSI element being visited
     */
    @Override
    public void visitElement(@NotNull PsiElement element) {
        if (element instanceof MethodReferenceImpl methodReference && Objects.equals(methodReference.getName(), ROUTE_METHOD_NAME)) {
            PsiElement nameParameter = methodReference.getParameter(0);

            if (this.isCorrectRoute(nameParameter)) {
                this.foundedRoute = methodReference;
            }
        }

        super.visitElement(element);
    }

    /**
     * Checks if the given name parameter matches the target route name
     * @param nameParameter The PSI element representing the route name parameter
     * @return              True if the route name matches, false otherwise
     */
    private boolean isCorrectRoute(PsiElement nameParameter) {
        return nameParameter instanceof StringLiteralExpression stringParameter
            && StrUtil.removeQuotes(stringParameter.getContents())
            .equals(
                StrUtil.removeQuotes(this.myElement.getText()
            )
        );
    }

    /**
     * Returns the found route element
     * @return The found route element, or null if not found
     */
    public PsiElement getFoundedRoute() {
        return this.foundedRoute;
    }
}

package at.alirezamoh.whisperer_for_laravel.routing.resourceRoute;

import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class resolves references controller actions and provides completion
 */
public class ResourceRouteActionReference extends PsiReferenceBase<PsiElement> {
    public ResourceRouteActionReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);
    }

    /**
     * Resolves the controller actions
     * @return The resolved controller action
     */
    @Override
    public @Nullable PsiElement resolve() {
        String targetAction = StrUtils.removeQuotes(myElement.getText());

        for (Method method : getAllControllerMethods())  {
            if (method.getName().equals(targetAction)){
                return method;
            }
        }

        return null;
    }

    /**
     * Returns an array of controller actions
     * @return middleware list
     */
    @Override
    public Object @NotNull [] getVariants() {
        List<LookupElementBuilder> variants = new ArrayList<>();

        for (Method method : getAllControllerMethods()) {
            variants.add(
                PsiElementUtils.buildSimpleLookupElement(method.getName())
            );
        }

        return variants.toArray();
    }

    /**
     * Collects all public controller methods from a resolved class reference.
     *
     * @return a list of public methods from the controller
     */
    private List<Method> getAllControllerMethods() {
        List<Method> methods = new ArrayList<>();

        PhpClass controllerClass = resolveControllerClass();
        if (controllerClass != null) {
            methods.addAll(PhpClassUtils.getClassPublicMethods(controllerClass, false));
        }

        return methods;
    }

    /**
     * Resolves the controller class from the method reference
     *
     * @return the resolved PhpClass representing the controller, or null if not found
     */
    private PhpClass resolveControllerClass() {
        MethodReference methodReference = MethodUtils.resolveMethodReference(myElement, 10);
        if (methodReference == null) {
            return null;
        }

        PsiElement firstChild = methodReference.getFirstChild();
        if (!(firstChild instanceof MethodReference resourceMethod)) {
            return null;
        }

        return PhpClassUtils.getCachedPhpClassFromClassConstant(resourceMethod.getParameter(1));
    }
}

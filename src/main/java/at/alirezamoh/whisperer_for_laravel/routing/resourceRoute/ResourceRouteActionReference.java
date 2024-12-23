package at.alirezamoh.whisperer_for_laravel.routing.resourceRoute;

import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.psiUtil.PsiUtil;
import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class resolves references controller actions and provides completion
 */
public class ResourceRouteActionReference extends PsiReferenceBase<PsiElement> {

    /**
     * List of method names to ignore when processing controller methods.
     */
    private final List<String> IGNORE_LIST = new ArrayList<>() {{
        add("__construct");
        add("__index");
        add("__invoke");
    }};

    public ResourceRouteActionReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);
    }

    /**
     * Resolves the controller actions
     * @return The resolved controller action
     */
    @Override
    public @Nullable PsiElement resolve() {
        String targetAction = StrUtil.removeQuotes(myElement.getText());

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
                PsiUtil.buildSimpleLookupElement(method.getName())
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
            methods.addAll(getPublicMethods(controllerClass));
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

        PsiElement parameter = resourceMethod.getParameter(1);
        if (!(parameter instanceof ClassConstantReference classConstantReference)) {
            return null;
        }

        PhpExpression classReference = classConstantReference.getClassReference();
        if (!(classReference instanceof ClassReferenceImpl)) {
            return null;
        }

        PsiReference reference = classReference.getReference();
        if (reference == null) {
            return null;
        }

        PsiElement resolved = reference.resolve();
        if (resolved instanceof PhpClass controllerClass) {
            return controllerClass;
        }

        return null;
    }

    /**
     * Retrieves all public methods from the given controller excluding ignored methods
     *
     * @param phpClass the controller to extract methods from
     * @return a list of public methods
     */
    private List<Method> getPublicMethods(PhpClass phpClass) {
        List<Method> publicMethods = new ArrayList<>();

        for (Method method : phpClass.getMethods()) {
            if (isEligibleMethod(method)) {
                publicMethods.add(method);
            }
        }

        return publicMethods;
    }

    /**
     * Checks if a method is eligible to be included in the results
     *
     * @param method the method to check
     * @return true or false
     */
    private boolean isEligibleMethod(Method method) {
        String methodName = method.getName();
        return method.getModifier().isPublic() &&
            !method.isAbstract() &&
            !IGNORE_LIST.contains(methodName);
    }
}

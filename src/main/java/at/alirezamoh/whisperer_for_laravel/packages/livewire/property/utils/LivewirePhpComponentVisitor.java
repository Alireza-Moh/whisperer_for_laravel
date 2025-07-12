package at.alirezamoh.whisperer_for_laravel.packages.livewire.property.utils;

import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LivewirePhpComponentVisitor extends PsiRecursiveElementVisitor {
    private final String VIEW_CLASS_NAME = "\\Illuminate\\Contracts\\View\\View";

    public List<PsiElement> properties = new ArrayList<>();

    private boolean ignoreWithMethod;

    public LivewirePhpComponentVisitor(boolean ignoreWithMethod) {
        this.ignoreWithMethod = ignoreWithMethod;
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {

        if (
            !ignoreWithMethod
            && element instanceof MethodReference methodReference
            && Objects.equals(methodReference.getName(), "with")
            && isCorrectWithMethod(methodReference)
        ) {
            extractProperties(methodReference);
        }

        if (element instanceof Field field) {
            if (field.getModifier().isPublic()) {
                properties.add(field);
            }
        }

        super.visitElement(element);
    }

    /**
     * Checks if the given <code>MethodReference</code> points to the
     * <code>with</code> method in the <code>View</code> class
     *
     * @param methodReference the method to check
     * @return true or false
     */
    private boolean isCorrectWithMethod(MethodReference methodReference) {
        PhpClass viewClass = PhpClassUtils.getCachedContainingPhpClassFromMethodRef(methodReference);

        return viewClass != null && Objects.equals(viewClass.getFQN(), VIEW_CLASS_NAME);
    }

    /**
     * Extracts properties from the first parameter of the
     * <code>with</code> method call
     *
     * @param methodReference reference to the <code>with</code> method call
     */
    private void extractProperties(MethodReference methodReference) {
        PsiElement parameter = methodReference.getParameter(0);

        if (parameter instanceof ArrayCreationExpression arrayCreationExpression) {
            for (ArrayHashElement arrayHashElement : arrayCreationExpression.getHashElements()) {
                PhpPsiElement key = arrayHashElement.getKey();
                if (key instanceof StringLiteralExpression stringLiteralExpression) {
                    properties.add(stringLiteralExpression);
                }
            }
        }

        if (parameter instanceof StringLiteralExpression stringLiteralExpression) {
            properties.add(stringLiteralExpression);
        }
    }
}

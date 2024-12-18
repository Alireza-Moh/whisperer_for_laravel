package at.alirezamoh.whisperer_for_laravel.config.visitors;

import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.impl.ArrayCreationExpressionImpl;
import com.jetbrains.php.lang.psi.elements.impl.ArrayHashElementImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpReturnImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Visits a PHP file to resolve a specific config key
 */
public class ConfigKeyVisitor extends PsiRecursiveElementWalkingVisitor {
    /**
     * The parts of the config key path
     */
    private String[] parts;

    /**
     * The resolved config key element
     */
    private PsiElement resolvedKey;

    /**
     * Visits an element in the config file
     * This method specifically checks for PhpReturn elements and attempts to resolve
     * the config key within the returned array
     * @param element The PSI element being visited
     */
    @Override
    public void visitElement(@NotNull PsiElement element) {
        if (element instanceof PhpReturnImpl phpReturn) {
            for (PsiElement child2 : phpReturn.getChildren()) {
                if (child2 instanceof ArrayCreationExpressionImpl arrayCreationExpressionImpl) {
                    this.resolvedKey = resolveConfigKey(arrayCreationExpressionImpl);
                }
            }
        }
        super.visitElement(element);
    }

    /**
     * Returns the resolved config key element
     * @return The resolved config key element, or null if not found
     */
    public PsiElement getResolvedKey() {
        return resolvedKey;
    }

    /**
     * Sets the parts of the config key path
     * @param parts The parts of the key path
     */
    public void setParts(String[] parts) {
        this.parts = parts;
    }

    /**
     * Resolves the config key within an array creation expression
     * This method handles both single-level and nested key scenarios
     * @param array The array creation expression
     * @return      The resolved config key element, or null if not found
     */
    private PsiElement resolveConfigKey(ArrayCreationExpressionImpl array) {
        if (parts == null || parts.length == 0) {
            return array;
        }

        if (parts.length == 1) {
            String currentKey = StrUtil.removeQuotes(parts[0]);
            ArrayHashElementImpl[] arrayHashElements = PsiTreeUtil.getChildrenOfType(array, ArrayHashElementImpl.class);
            if (arrayHashElements != null) {
                for (ArrayHashElementImpl arrayHashElement : arrayHashElements) {
                    PsiElement keyElement = arrayHashElement.getKey();
                    if (keyElement != null && StrUtil.removeQuotes(keyElement.getText()).equals(currentKey)) {
                        return arrayHashElement;
                    }
                }
            }
            return null;
        } else {
            return this.resolveNestedArray(array, 0);
        }
    }

    /**
     * Recursively resolves nested config keys within an array
     * @param array The array creation expression
     * @param index The current index in the key path
     * @return      The resolved config key element, or null if not found
     */
    private PsiElement resolveNestedArray(ArrayCreationExpressionImpl array, int index) {
        if (index >= this.parts.length) {
            return null;

        }

        String currentKey = StrUtil.removeQuotes(this.parts[index]);
        ArrayHashElementImpl[] arrayHashElements = PsiTreeUtil.getChildrenOfType(array, ArrayHashElementImpl.class);

        if (arrayHashElements != null) {
            for (ArrayHashElementImpl arrayHashElement : arrayHashElements) {
                PsiElement keyElement = arrayHashElement.getKey();
                if (keyElement != null && StrUtil.removeQuotes(keyElement.getText()).equals(currentKey)) {
                    if (index == this.parts.length - 1) {
                        return arrayHashElement;
                    }

                    PhpPsiElement valueElement = arrayHashElement.getValue();
                    if (valueElement instanceof ArrayCreationExpressionImpl) {
                        return resolveNestedArray((ArrayCreationExpressionImpl) valueElement, index + 1);
                    }
                }
            }
        }
        return null;
    }
}


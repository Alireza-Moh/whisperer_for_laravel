package at.alirezamoh.whisperer_for_laravel.config.visitors;

import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.jetbrains.php.lang.psi.elements.impl.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Recursively visits a PHP file to collect config keys from array return statements
 */
public class ArrayReturnPsiRecursiveVisitor extends PsiRecursiveElementWalkingVisitor {
    /**
     * List of the collected config keys
     */
    private Map<String, String> variants = new HashMap<>();

    /**
     * The parent keys leading to the current config key
     */
    private String parentKey;

    /**
     * @param parentKey      The parent keys leading to the current config key
     */
    public ArrayReturnPsiRecursiveVisitor(String parentKey) {
        this.parentKey = parentKey;
    }

    /**
     * Visits an element in the config file
     * @param element The PSI element being visited
     */
    @Override
    public void visitElement(@NotNull PsiElement element) {
        if(element instanceof PhpReturn phpReturn) {
            this.visitPhpReturn(phpReturn);
        }
        super.visitElement(element);
    }

    /**
     * Visits a PhpReturn element to extract config keys from its array creation expression
     * @param phpReturn The PhpReturn element being visited
     */
    public void visitPhpReturn(PhpReturn phpReturn) {
        PsiElement arrayCreation = phpReturn.getFirstPsiChild();

        if(arrayCreation instanceof ArrayCreationExpression arrayCreationExpression) {
            collectConfigKeys(arrayCreationExpression);
        }
    }

    public Map<String, String> getVariants() {
        return variants;
    }

    /**
     * Collects config keys from an array
     * It iterates through the hash elements of the array and adds them as lookups
     * @param arrayCreationExpression The array creation expression
     */
    private void collectConfigKeys(ArrayCreationExpression arrayCreationExpression) {
        List<ArrayHashElement> hashElements = PsiTreeUtil.getChildrenOfTypeAsList(arrayCreationExpression, ArrayHashElement.class);

        for(ArrayHashElement hashElement: hashElements) {
            this.addVariant(hashElement, this.parentKey);
        }
    }

    /**
     * Adds a config key as a variant
     * This method handles nested arrays and generates the full key path
     * @param hashElement The ArrayHashElement representing the config key
     * @param parentKey The parent keys leading to the current config key
     */
    private void addVariant(@NotNull ArrayHashElement hashElement, String parentKey) {
        String key = this.getHashKey(hashElement);
        if (key.isEmpty()) {
            return;
        }

        String fullKey = this.generateFullKey(parentKey, key);
        PhpPsiElement value = hashElement.getValue();

        if (value instanceof ArrayCreationExpressionImpl myArrayCreationExpressionImpl) {
            iterateOverArrayCreationExpression(
                myArrayCreationExpressionImpl,
                this.getKeyWithParent(parentKey, key)
            );
            variants.put(fullKey, "");
        } else {
            variants.put(fullKey, this.getValueAsString(value));
        }
    }

    /**
     * Extracts the key from an ArrayHashElement
     * @param hashElement The ArrayHashElement
     * @return The extracted key
     */
    private String getHashKey(ArrayHashElement hashElement) {
        PsiElement key = hashElement.getKey();
        if
        (
            key instanceof FunctionReferenceImpl
            || key instanceof TernaryExpressionImpl
            || key instanceof ClassConstantReferenceImpl
            || key instanceof UnaryExpressionImpl
            || key instanceof MethodReferenceImpl
            || key instanceof ConcatenationExpressionImpl
        )
        {
            return "";
        }
        return Objects.requireNonNull(key).getText();
    }

    /**
     * Generates the full key path for a config key
     * @param parentKey  The parent keys
     * @param key         The current key
     * @return            The full key path
     */
    private String generateFullKey(String parentKey, String key) {
        String prefixedKey = parentKey.isEmpty() ? "" : parentKey + ".";

        return StrUtils.removeQuotes(prefixedKey + key);
    }

    /**
     * Iterates over the hash elements of a nested array creation expression
     * @param arrayExpression The array creation expression
     * @param parentKey     The parent keys
     */
    private void iterateOverArrayCreationExpression(
        ArrayCreationExpressionImpl arrayExpression,
        String parentKey
    ) {
        for (ArrayHashElement hashElement : arrayExpression.getHashElements()) {
            this.addVariant(hashElement, parentKey);
        }
    }

    /**
     * Combines the parent keys with the current key
     * @param parentKeys The parent keys
     * @param key        The current key
     * @return           The combined key path
     */
    private String getKeyWithParent(String parentKeys, String key) {
        return parentKeys.isEmpty() ? key : parentKeys + "." + key;
    }

    /**
     * Retrieves the value of a config key as a string
     * This method handles various PSI element types and formats the value accordingly
     * If the value is a complex expression, it returns an empty string
     * @param valueElement The PSI element representing the config value
     * @return             The formatted value as a string
     */
    private String getValueAsString(PsiElement valueElement) {
        if
        (
            valueElement instanceof FunctionReferenceImpl
            || valueElement instanceof TernaryExpressionImpl
            || valueElement instanceof ClassConstantReferenceImpl
            || valueElement instanceof UnaryExpressionImpl
            || valueElement instanceof MethodReferenceImpl
            || valueElement instanceof ConcatenationExpressionImpl
        )
        {
            return "";
        }

        String value = valueElement.getText();
        if (!value.isEmpty()) {
            value = StrUtils.removeQuotes(value);
        }
        return value;
    }
}

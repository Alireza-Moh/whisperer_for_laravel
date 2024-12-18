package at.alirezamoh.whisperer_for_laravel.config.visitors;

import at.alirezamoh.whisperer_for_laravel.support.WhispererForLaravelIcon;
import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
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
    private List<LookupElementBuilder> suggestions = new ArrayList<>();

    /**
     * Map of ArrayHashElement objects and their corresponding config key strings
     */
    private Map<ArrayHashElement, String> suggestionsWithCorrectPsiElement = new HashMap<>();

    /**
     * The name of the directory containing the config file
     */
    private String dirName;

    /**
     * The name of the config file
     */
    private String configFileName;

    /**
     * The parent keys leading to the current config key
     */
    private String parentKeys;

    /**
     * The namespace for the config keys
     */
    private String configNamespace;

    private boolean forModule;

    /**
     * @param dirName         The name of the directory containing the config file
     * @param configFileName  The name of the config file
     * @param parentKeys      The parent keys leading to the current config key
     * @param configNamespace The namespace for the config keys
     * @param forModule       Should append file name to the string
     */
    public ArrayReturnPsiRecursiveVisitor(String dirName, String configFileName, String parentKeys, String configNamespace, boolean forModule) {
        this.dirName = dirName;
        this.configFileName = configFileName;
        this.parentKeys = parentKeys;
        this.configNamespace = configNamespace;
        this.forModule = forModule;
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

    /**
     * Returns the list of the collected config keys
     * @return config keys
     */
    public List<LookupElementBuilder> getSuggestions() {
        return suggestions;
    }

    /**
     * Returns a map of ArrayHashElement objects and their corresponding config key strings
     * @return The map of ArrayHashElement objects and config key strings
     */
    public Map<ArrayHashElement, String> getSuggestionsWithCorrectPsiElement() {
        return suggestionsWithCorrectPsiElement;
    }

    /**
     * Builds a LookupElementBuilder for a config key
     * @param key   The config key
     * @param value The config value
     * @return      The LookupElementBuilder
     */
    public LookupElementBuilder buildLookupElement(String key, String value) {
        return LookupElementBuilder
                .create(key)
                .withLookupString(key)
                .withPresentableText(key)
                .withTailText(value, true)
                .bold()
                .withIcon(WhispererForLaravelIcon.LARAVEL_ICON);
    }

    /**
     * Collects config keys from an array
     * It iterates through the hash elements of the array and adds them as lookups
     * @param arrayCreationExpression The array creation expression
     */
    private void collectConfigKeys(ArrayCreationExpression arrayCreationExpression) {
        List<ArrayHashElement> hashElements = PsiTreeUtil.getChildrenOfTypeAsList(arrayCreationExpression, ArrayHashElement.class);

        for(ArrayHashElement hashElement: hashElements) {
            this.addLookup(hashElement, this.dirName, this.configFileName, this.parentKeys);
        }
    }

    /**
     * Adds a config key as a lookup element
     * This method handles nested arrays and generates the full key path
     * @param hashElement The ArrayHashElement representing the config key
     * @param dirName     The name of the directory containing the config file
     * @param filename    The name of the config file
     * @param parentKeys  The parent keys leading to the current config key
     */
    private void addLookup(@NotNull ArrayHashElement hashElement, String dirName, String filename, String parentKeys) {
        String key = this.getHashKey(hashElement);
        String fullKey = this.generateFullKey(dirName, filename, parentKeys, key);

        if (!this.configNamespace.isEmpty()) {
            fullKey = this.configNamespace + "." + fullKey;
        }
        PhpPsiElement value = hashElement.getValue();

        if (value instanceof ArrayCreationExpressionImpl myArrayCreationExpressionImpl) {
            iterateOverArrayCreationExpression(
                dirName,
                myArrayCreationExpressionImpl,
                this.getKeyWithParent(parentKeys, key),
                filename
            );
            this.suggestions.add(
                this.buildLookupElement(fullKey, "")
            );
        } else {
            this.suggestions.add(
                this.buildLookupElement(fullKey, this.getValueAsString(value))
            );
        }

        this.suggestionsWithCorrectPsiElement.put(hashElement, fullKey);
    }

    /**
     * Extracts the key from an ArrayHashElement
     * @param hashElement The ArrayHashElement
     * @return The extracted key
     */
    private String getHashKey(ArrayHashElement hashElement) {
        return Objects.requireNonNull(hashElement.getKey()).getText();
    }

    /**
     * Generates the full key path for a config key
     * @param dirName     The directory name
     * @param filename    The file name
     * @param parentKeys  The parent keys
     * @param key         The current key
     * @return            The full key path
     */
    private String generateFullKey(String dirName, String filename, String parentKeys, String key) {
        String prefixedKey = parentKeys.isEmpty() ? "" : parentKeys + ".";

        if (forModule) {
            return StrUtil.removeQuotes(prefixedKey + key);
        }
        else {
            if (dirName.isEmpty()) {
                return StrUtil.removeQuotes(filename + "." + prefixedKey + key);
            }
            else {
                return StrUtil.removeQuotes(dirName + "." + filename + "." + prefixedKey + key);
            }
        }
    }

    /**
     * Iterates over the hash elements of a nested array creation expression
     * @param dirName         The directory name
     * @param arrayExpression The array creation expression
     * @param parentKeys      The parent keys
     * @param filename        The file name
     */
    private void iterateOverArrayCreationExpression(
        String dirName,
        ArrayCreationExpressionImpl arrayExpression,
        String parentKeys,
        String filename
    ) {
        for (ArrayHashElement hashElement : arrayExpression.getHashElements()) {
            this.addLookup(hashElement, dirName, filename, parentKeys);
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
            value = " = " + StrUtil.removeQuotes(value);
        }
        return value;
    }
}

package at.alirezamoh.whisperer_for_laravel.support.utils;

import at.alirezamoh.whisperer_for_laravel.support.WhispererForLaravelIcon;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.ArrayHashElementImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class PsiElementUtils {
    /**
     * Builds a LookupElementBuilder
     *
     * @param key The key
     * @return The LookupElementBuilder
     */
    public static @NotNull LookupElementBuilder buildSimpleLookupElement(String key) {
        return LookupElementBuilder
            .create(key)
            .withLookupString(key)
            .bold()
            .withIcon(WhispererForLaravelIcon.LARAVEL_ICON);
    }

    /**
     * Builds a LookupElementBuilder
     *
     * @param lookupElementBuilder The key
     * @return The LookupElementBuilder
     */
    public static @NotNull LookupElement buildPrioritizedLookupElement(LookupElementBuilder lookupElementBuilder, int priority) {
        return PrioritizedLookupElement.withPriority(
            lookupElementBuilder,
            priority
        );
    }

    /**
     * Returns the start offset of a text string, excluding any leading or trailing quotes
     * @param stringLiteralExpression The text string
     * @return     The start offset
     */
    public static int getStartOffset(StringLiteralExpression stringLiteralExpression) {
        return stringLiteralExpression.getValueRange().getStartOffset();
    }

    /**
     * Returns the end offset of a text string, excluding any leading or trailing quotes
     * @param stringLiteralExpression The text string
     * @return     The end offset
     */
    public static int getEndOffset(StringLiteralExpression stringLiteralExpression) {
        return stringLiteralExpression.getValueRange().getEndOffset();
    }

    /**
     * Retrieves the value of a parameter at a specific index from a MethodReference
     *
     * @param methodReference The method reference containing the parameter list
     * @param index The index of the parameter to retrieve
     * @return The value of the parameter at the given index, or null if not found
     * <p>
     * Example:
     * For a method call `someMethod('param1', 'param2')`, calling this with index 1
     * will return `"param2"`
     */
    public static @Nullable String getMethodParameterAt(@NotNull MethodReference methodReference, int index) {

        ParameterList parameterList = methodReference.getParameterList();
        if(parameterList == null) {
            return null;
        }

        return getMethodParameterAt(parameterList, index);
    }

    /**
     * Retrieves the value of a parameter at a specific index from a ParameterList
     *
     * @param parameterList The parameter list
     * @param index The index of the parameter to retrieve
     * @return The string value of the parameter at the given index, or null if not found
     */
    public static @Nullable String getMethodParameterAt(@NotNull ParameterList parameterList, int index) {
        PsiElement[] parameters = parameterList.getParameters();

        if(parameters.length < index + 1) {
            return null;
        }

        return getMethodParameter(parameters[index]);
    }

    /**
     * Retrieves the text value of a method parameter
     *
     * @param parameter The parameter to evaluate
     * @return The extracted string value, or null
     */
    public static String getMethodParameter(PsiElement parameter) {

        if (!(parameter instanceof StringLiteralExpression stringLiteralExpression)) {
            return null;
        }

        String stringValue = stringLiteralExpression.getText();
        String value = stringValue.substring(
            stringLiteralExpression.getValueRange().getStartOffset(),
            stringLiteralExpression.getValueRange().getEndOffset()
        );

        return removeIdeaRuleHack(value);
    }

    /**
     * Removes IntelliJ-specific markers (e.g., "IntellijIdeaRulezzz") from a string
     *
     * @param value The input string to clean.
     * @return A cleaned string without IntelliJ markers.
     */
    public static String removeIdeaRuleHack(String value) {
        String result = value.replace("IntellijIdeaRulezzz", "")
            .replace("IntellijIdeaRulezzz ", "")
            .trim();

        return StrUtils.removeQuotes(result);
    }

    /**
     * Checks if the given PSI element is a value in an array [key => value]
     *
     * @param element  The PSI element to check
     * @param maxDepth Maximum number of parents to traverse up the PSI tree
     * @return true or false
     */
    public static boolean isInArrayValue(PsiElement element, int maxDepth) {
        PsiElement currentElement = element;
        int currentDepth = 0;

        while (currentElement != null && currentDepth < maxDepth) {
            if (currentElement instanceof ArrayHashElementImpl arrayHashElement) {
                if (arrayHashElement.getValue() == element) {
                    return true;
                }
            }

            currentElement = currentElement.getParent();
            currentDepth++;
        }

        return false;
    }

    /**
     * Determines if the given PSI element is part of an associative array
     *
     * @param element  The PSI element to check
     * @param maxDepth Maximum number of parents to traverse up the PSI tree
     * @return true or false
     */
    public static boolean isAssocArray(PsiElement element, int maxDepth) {
        PsiElement currentElement = element;
        int depth = 0;

        while (currentElement != null && depth <= maxDepth) {
            if (currentElement instanceof ArrayCreationExpression arrayCreationExpression) {
                Iterable<ArrayHashElement> iterator = arrayCreationExpression.getHashElements();

                boolean s = iterator.iterator().hasNext();
                return iterator.iterator().hasNext();
            }
            currentElement = currentElement.getParent();
            depth++;
        }

        return false;
    }

    /**
     * Determines if the given PSI element is part of a "regular" array [1, 2, "a"]
     *
     * @param element  The PSI element to check
     * @param maxDepth Maximum number of parents to traverse up the PSI tree
     * @return true or false
     */
    public static boolean isRegularArray(PsiElement element, int maxDepth) {
        PsiElement currentElement = element;
        int depth = 0;

        while (currentElement != null && depth <= maxDepth) {
            if (currentElement instanceof ArrayCreationExpression arrayCreationExpression) {
                Iterable<ArrayHashElement> iterator = arrayCreationExpression.getHashElements();

                // If there are no hash elements, or no next element, it's a regular array
                if (iterator == null || !iterator.iterator().hasNext()) {
                    return true;
                }

                return false;
            }
            currentElement = currentElement.getParent();
            depth++;
        }

        return false;
    }

    /**
     * Retrieves an {@link ArrayCreationExpression} representing an array
     *
     * @param element  The PSI element to start from
     * @param maxDepth Maximum number of parents to traverse up the PSI tree
     * @return The found {@link ArrayCreationExpression}, or {@code null} if not found or if it contains key-value pairs
     */
    public static @Nullable ArrayCreationExpression getRegularArray(PsiElement element, int maxDepth) {
        PsiElement currentElement = element;
        int depth = 0;

        while (currentElement != null && depth <= maxDepth) {
            if (currentElement instanceof ArrayCreationExpression arrayCreationExpression) {
                Iterable<ArrayHashElement> iterator = arrayCreationExpression.getHashElements();

                if (iterator == null || !iterator.iterator().hasNext()) {
                    return arrayCreationExpression;
                }

                return null;
            }
            currentElement = currentElement.getParent();
            depth++;
        }

        return null;
    }

    /**
     * Resolves a file path to a {@link VirtualFile} using the local file system
     *
     * @param filePath A non-null string representing the file path to resolve.
     * @return The corresponding {@link VirtualFile}, or {@code null} if the file was not found
     */
    public static @Nullable VirtualFile resolveFilePath(@NotNull String filePath) {
        return LocalFileSystem.getInstance().findFileByPath(filePath);
    }

    /**
     * Retrieves the PSIFile of a given {@link VirtualFile}.
     *
     * @param file    The {@link VirtualFile} for which to get the PSI file
     * @param project The current project
     * @return The corresponding {@link PsiFile}, or {@code null} if it could not be resolved
     */
    public static @Nullable PsiFile resolvePsiFile(@NotNull VirtualFile file, @NotNull Project project) {
        return PsiManager.getInstance(project).findFile(file);
    }
}

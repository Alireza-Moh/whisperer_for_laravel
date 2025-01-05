package at.alirezamoh.whisperer_for_laravel.support.utils;

import at.alirezamoh.whisperer_for_laravel.support.WhispererForLaravelIcon;
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
     * Builds a LookupElementBuilder for a config key
     *
     * @param key The config key
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

    public static boolean isRegularArray(PsiElement element, int maxDepth) {
        PsiElement currentElement = element;
        int depth = 0;

        while (currentElement != null && depth <= maxDepth) {
            if (currentElement instanceof ArrayCreationExpression arrayCreationExpression) {
                Iterable<ArrayHashElement> iterator = arrayCreationExpression.getHashElements();

                if (iterator == null) {
                    return true;
                } else if (!iterator.iterator().hasNext()) {
                    return true;
                }

                return false;
            }
            currentElement = currentElement.getParent();
            depth++;
        }

        return false;
    }

    public static @Nullable ArrayCreationExpression getRegularArray(PsiElement element, int maxDepth) {
        PsiElement currentElement = element;
        int depth = 0;

        while (currentElement != null && depth <= maxDepth) {
            if (currentElement instanceof ArrayCreationExpression arrayCreationExpression) {
                Iterable<ArrayHashElement> iterator = arrayCreationExpression.getHashElements();

                if (iterator == null) {
                    return arrayCreationExpression;
                } else if (!iterator.iterator().hasNext()) {
                    return arrayCreationExpression;
                }

                return null;
            }
            currentElement = currentElement.getParent();
            depth++;
        }

        return null;
    }

    public static @Nullable VirtualFile resolveFilePath(@NotNull String filePath) {
        return LocalFileSystem.getInstance().findFileByPath(filePath);
    }

    public static @Nullable PsiFile resolvePsiFile(@NotNull VirtualFile file, @NotNull Project project) {
        return PsiManager.getInstance(project).findFile(file);
    }
}

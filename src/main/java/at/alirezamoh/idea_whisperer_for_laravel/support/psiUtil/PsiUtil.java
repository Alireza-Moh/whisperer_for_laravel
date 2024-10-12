package at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil;

import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

/**
 * Provides utility methods for working with PSI elements
 * This class offers functionalities for checking the position of the caret within
 * function and method calls, determining if an element is inside a specific function
 * or method, and extracting information from PSI elements
 */
public class PsiUtil {
    /**
     * Checks if a PSI element is inside a function call with a specific name
     * @param element     The PSI element to check
     * @param methodName  The name of the function
     * @return            True if the element is inside the function, false otherwise
     */
    public static boolean isInsideFunction(PsiElement element, String methodName) {
        FunctionReferenceImpl function = PsiTreeUtil.getParentOfType(element, FunctionReferenceImpl.class);

        return function != null && Objects.equals(function.getName(), methodName);
    }

    /**
     * Checks if a PSI element is inside a method call with a specific name and class namespace
     * @param element              The PSI element to check
     * @param methodNames          The array of method names to check
     * @param classNamespaceName   The namespace of the class containing the method
     * @return                     True if the element is inside the method, false otherwise
     */
    public static boolean isInsideMethod(PsiElement element, String[] methodNames, String classNamespaceName) {
        MethodReferenceImpl methodCall = getMethodReferenceImpl(element);

        if (methodCall != null && Arrays.asList(methodNames).contains(methodCall.getName())) {
            PhpExpression requestMethodClassRef = methodCall.getClassReference();

            return requestMethodClassRef != null
                && Objects.equals(requestMethodClassRef.getGlobalType().toString(), classNamespaceName);
        }
        return false;
    }

    /**
     * Returns the start offset of a text string, excluding any leading or trailing quotes
     * @param text The text string
     * @return     The start offset
     */
    public static int getStartOffset(String text) {
        return (text.startsWith("\"") || text.startsWith("'")) ? 1 : 0;
    }

    /**
     * Returns the end offset of a text string, excluding any leading or trailing quotes
     * @param text The text string
     * @return     The end offset
     */
    public static int getEndOffset(String text) {
        return (text.endsWith("\"") || text.startsWith("'")) ? text.length() - 1 : text.length();
    }

    /**
     * Retrieves the MethodReferenceImpl containing a given PSI element
     * @param element The PSI element
     * @return        The MethodReferenceImpl, or null if not found
     */
    public static MethodReferenceImpl getMethodReferenceImpl(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, MethodReferenceImpl.class);
    }

    /**
     * Checks if the caret is within the first parameter of a method call
     * @param element The PSI element to check
     * @return        True if the caret is in the first parameter, false otherwise
     */
    public static boolean isCaretInMethodFirstParameter(PsiElement element) {
        MethodReferenceImpl method = PsiTreeUtil.getParentOfType(element, MethodReferenceImpl.class);

        if (method != null) {
            PsiElement[] parameters = method.getParameters();
            return parameters.length > 0 && PsiUtil.isInRange(parameters[0], getOffset(element));
        }
        return false;
    }

    /**
     * Checks if the caret is within the first parameter of a function call
     * @param element The PSI element to check
     * @return        True if the caret is in the first parameter, false otherwise
     */
    public static boolean isCaretInFunctionFirstParameter(PsiElement element) {
        FunctionReferenceImpl method = PsiTreeUtil.getParentOfType(element, FunctionReferenceImpl.class);

        if (method != null) {
            PsiElement[] parameters = method.getParameters();
            return parameters.length > 0 && isInRange(parameters[0], getOffset(element));
        }
        return false;
    }

    /**
     * Checks if an offset is within the text range of a PSI element
     * @param firstParameter The PSI element
     * @param offset         The offset to check
     * @return               True if the offset is within the element's range, false otherwise
     */
    public static boolean isInRange(PsiElement firstParameter, int offset) {
        TextRange parameterRange = firstParameter.getTextRange();
        return parameterRange.contains(offset);
    }

    /**
     * Extracts the first parameter from a method
     * @param method method reference
     * @return the first parameter value
     */
    public static String getFirstParameterFromMethod(MethodReference method) {
        String key = "";

        ParameterList parameters = method.getParameterList();
        if (parameters != null) {
            PsiElement namespaceParameter = parameters.getParameter(0);
            if (namespaceParameter != null) {
                key = StrUtil.removeQuotes(namespaceParameter.getText());
            }
        }

        return key;
    }

    /**
     * Extracts the second parameter from a method
     * @param method method reference
     * @return the second parameter value
     */
    public static @Nullable String getSecondParameterFromMethod(MethodReference method) {
        String key = null;

        ParameterList parameters = method.getParameterList();
        if (parameters != null) {
            PsiElement namespaceParameter = parameters.getParameter(1);
            if (namespaceParameter != null) {
                key = StrUtil.removeQuotes(namespaceParameter.getText());
            }
        }

        return key;
    }

    /**
     * Retrieves the current caret offset in the editor
     * @param element The PSI element
     * @return        The caret offset
     */
    private static int getOffset(PsiElement element) {
        Editor editor = FileEditorManager.getInstance(element.getProject()).getSelectedTextEditor();

        if (editor != null) {
            return editor.getCaretModel().getOffset();
        }
        return 0;
    }
}

package at.alirezamoh.whisperer_for_laravel.translation.util;

import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TranslationUtil {
    /**
     * The names of the translation helper functions
     */
    private static final Map<String, Integer> TRANSLATION_METHODS = new HashMap<>() {{
        put("__", 0);
        put("trans_choice", 0);
    }};

    /**
     * Checks if the given PSI element is inside a translation helper function call
     * @param psiElement The PSI element to check
     * @return           True or false
     */
    public static boolean isInsideCorrectMethod(@NotNull PsiElement psiElement) {
        FunctionReferenceImpl function = MethodUtils.resolveFunctionReference(psiElement, 10);

        return function != null && isTranslationParam(function, psiElement);
    }

    /**
     * Check if the given reference and position match the translation name parameter criteria
     * @param reference The function reference
     * @param position The PSI element position
     * @return True or false
     */
    private static boolean isTranslationParam(FunctionReferenceImpl reference, PsiElement position) {
        String referenceName = reference.getName();

        if (referenceName == null) {
            return false;
        }

        Integer expectedParamIndex = TRANSLATION_METHODS.get(referenceName);
        if (expectedParamIndex == null) {
            return false;
        }

        return MethodUtils.findParamIndex(position, false) == expectedParamIndex;
    }
}

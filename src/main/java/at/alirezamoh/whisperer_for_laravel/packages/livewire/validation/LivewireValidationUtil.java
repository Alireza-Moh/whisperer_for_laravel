package at.alirezamoh.whisperer_for_laravel.packages.livewire.validation;

import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LivewireValidationUtil {
    /**
     * Check if the psi element is inside the validate attribute
     * @param psiElement target element
     * @return true or false
     */
    public static boolean isInsideCorrectAttribute(PsiElement psiElement, int parameterPosition) {
        PhpAttribute validateAttribute = getPhpAttribute(psiElement);

        return validateAttribute != null
            && Objects.requireNonNull(validateAttribute.getFQN()).equals("\\Livewire\\Attributes\\Validate")
            && isRuleParam(psiElement, parameterPosition);
    }

    public static @Nullable PhpAttribute getPhpAttribute(PsiElement psiElement) {
        PsiElement currentElement = psiElement;
        PhpAttribute validateAttribute = null;
        while (currentElement != null) {
            if (currentElement instanceof PhpAttribute phpAttribute) {
                validateAttribute = phpAttribute;
                break;
            }

            currentElement = currentElement.getParent();
        }
        return validateAttribute;
    }

    public static boolean isRuleParam(PsiElement position, int parameterPosition) {
        return MethodUtils.findParamIndex(position, false) == parameterPosition;
    }
}

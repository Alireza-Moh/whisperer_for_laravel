package at.alirezamoh.whisperer_for_laravel.eloquent.factroy;

import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.Nullable;

final public class FactoryHelper {
    /**
     * Extracts the model name from the specified factory class
     * The method checks for a field named "model" and, if found, extracts the class name
     * if not, it falls back to inferring the model name from the factory class name
     *
     * @param factory the factory {@code PhpClass} to inspect
     * @return the extracted model name, or {@code null} if not available
     */
    public static @Nullable String getModelNameFromClassAttribute(PhpClass factory) {
        Field modelField = factory.findOwnFieldByName("model", false);

        if (modelField != null) {
            PsiElement defaultValue = modelField.getDefaultValue();
            if (defaultValue instanceof ClassConstantReference classConstantReference) {
                PhpExpression reference = classConstantReference.getClassReference();
                if (reference instanceof ClassReference classReference) {
                    return StrUtils.removeQuotes(classReference.getName());
                }
            }
        }

        return null;
    }
}

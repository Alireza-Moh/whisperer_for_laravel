package at.alirezamoh.whisperer_for_laravel.support.codeGeneration;

import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.ArrayCreationExpressionImpl;
import com.jetbrains.php.lang.psi.elements.impl.ClassConstantReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpReturnImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to resolve eloquent model attribute casts
 * <p>
 * This class inspects the {@code casts()} method within a model class
 * and extracts the array of attribute names to their cast types
 */

public class ModelCastsResolver {
    /**
     * Resolves the casted attributes from the given eloquent model class
     * <p>
     * It inspects the {@code casts()} method and returns a map of attribute names to class cast fqn
     *
     * @param modelClass the PHP class representing the eloquent model
     * @return a map of field names to cast class FQNs, or an empty map if no casts are defined
     */
    public static Map<String, String> resolveCasts(@NotNull PhpClass modelClass) {
        Method castsMethod = modelClass.findMethodByName("casts");
        if (castsMethod == null) {
            return Collections.emptyMap();
        }

        ArrayCreationExpressionImpl arrayCasts = findArrayOfCasts(castsMethod);
        if (arrayCasts == null) {
            return Collections.emptyMap();
        }

        return extractFieldsFromCastArray(arrayCasts);
    }

    /**
     * Locates the array returned by the {@code casts()} method
     * <p>
     * This method searches for a {@link PhpReturnImpl} and extracts the {@link ArrayCreationExpressionImpl} from it
     *
     * @param method the method to analyze
     * @return the array creation expression containing cast definitions, or {@code null} if not found
     */
    private static @Nullable ArrayCreationExpressionImpl findArrayOfCasts(@NotNull Method method) {
        PhpReturnImpl phpReturn = PsiTreeUtil.findChildOfType(method, PhpReturnImpl.class);
        if (phpReturn == null) {
            return null;
        }

        return PsiTreeUtil.findChildOfType(phpReturn, ArrayCreationExpressionImpl.class);
    }

    /**
     * Extracts field-to-cast mappings from the given array
     * <p>
     * Each array item is expected to be in the form {@code 'field' => SomeCast::class}.
     *
     * @param arrayExpr the array containing cast definitions
     * @return a map of field names to fqn cast class names
     */
    private static Map<String, String> extractFieldsFromCastArray(@NotNull ArrayCreationExpressionImpl arrayExpr) {
        Map<String, String> casts = new HashMap<>();
        for (ArrayHashElement element : arrayExpr.getHashElements()) {
            PsiElement key = element.getKey();
            PsiElement value = element.getValue();

            if (!(key instanceof StringLiteralExpression strKey)) {
                continue;
            }

            if (!(value instanceof ClassConstantReferenceImpl constRef)) {
                continue;
            }

            PhpExpression ref = constRef.getClassReference();
            if (!(ref instanceof ClassReferenceImpl classRef)) {
                continue;
            }

            casts.put(
                StrUtils.removeQuotes(strKey.getText()),
                classRef.getFQN()
            );
        }

        return casts;
    }
}

package at.alirezamoh.whisperer_for_laravel.eloquent.factroy;

import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.whisperer_for_laravel.support.utils.EloquentUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Extracts Eloquent model fields from a factory's definition() method or the @extends doc tag
 * This class is used to resolve the fields of an Eloquent model
 * when working with Laravel factories
 */
public class EloquentModelFieldExtractorInFactory {

    private static Project project;

    /**
     * Extracts the fields of an Eloquent model from a factory's definition method
     * or from the @extends tag in the factory's PHPDoc
     *
     * @param factoryClass The factory class to inspect
     * @param project    The current project instance
     * @return A list of {@link Field} objects representing the model's fields, or null if not found
     */
    public static @Nullable List<Field> extract(PhpClass factoryClass, Project project) {
        EloquentModelFieldExtractorInFactory.project = project;

        // Try protected $model = MyModel::class; attribute first
        String modelName = FactoryHelper.getModelNameFromClassAttribute(factoryClass);
        if (modelName != null) {
            return fetchFields(modelName);
        }

        // Fallback: scan PHPDoc for @extend tag
        modelName = resolveModelNameFromPhpDocExtendTag(factoryClass);
        if (modelName != null) {
            return fetchFields(modelName);
        }

        return null;
    }

    /**
     * Resolves the model name from the @extends tag in the factory's PHPDoc
     * This method scans the factory class's PHPDoc for an @extends tag
     * and extracts the first type name found within it
     *
     * @param factoryClass The factory class to inspect
     * @return The model name if found, or null if not found
     */
    public @Nullable static String resolveModelNameFromPhpDocExtendTag(PhpClass factoryClass) {
        PhpDocComment doc = factoryClass.getDocComment();
        if (doc == null) {
            return null;
        }

        PhpDocTag[] tags = doc.getTagElementsByName("@extends");

        for (PhpDocTag tag : tags) {
            String name = extractFirstTypeName(tag);
            if (name != null) {
                return name;
            }
        }
        return null;
    }

    /**
     * Fetches the fields of the specified Eloquent model
     *
     * @param eloquentModelFQN The fully qualified name of the Eloquent model
     * @return A list of {@link Field} objects representing the model's fields
     */
    private @NotNull static List<Field> fetchFields(String eloquentModelFQN) {
        return EloquentUtils.getFields(eloquentModelFQN, true, project);
    }

    /**
     * Finds the first simple type name inside an @extends tag
     *
     * @param tag The @extends tag to inspect
     */
    private @Nullable static String extractFirstTypeName(PhpDocTag tag) {
        for (PsiElement child : tag.getChildren()) {
            if (child instanceof PhpDocTypeImpl) {
                for (PsiElement inner : child.getChildren()) {
                    if (inner instanceof PhpPsiElement) {
                        for (PsiElement inner2 : inner.getChildren()) {
                            if (inner2 instanceof PhpDocTypeImpl firstDocTypeImpl) {
                                return firstDocTypeImpl.getName();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}

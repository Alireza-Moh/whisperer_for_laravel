package at.alirezamoh.whisperer_for_laravel.eloquent.resource;

import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.whisperer_for_laravel.support.utils.EloquentUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Extracts Eloquent model fields from a resource's @mixin doc tag
 * This class is used to resolve the fields of an Eloquent model
 * when working with Laravel factories
 */
public class EloquentModelFieldExtractorInResource {

    private static Project project;

    /**
     * Extracts the fields of an Eloquent model from a resource's @mixin doc tag
     *
     * @param resourceClass The factory class to inspect
     * @param project    The current project instance
     * @return A list of {@link Field} objects representing the model's fields, or null if not found
     */
    public static @Nullable List<Field> extract(PhpClass resourceClass, Project project) {
        EloquentModelFieldExtractorInResource.project = project;

        // Fallback: scan PHPDoc for @mixin tag
        String modelName = resolveModelNameFromPhpDocMixinTag(resourceClass);
        if (modelName != null) {
            return fetchFields(modelName);
        }

        return null;
    }

    /**
     * Resolves the model name from the @mixin tag in the resource's PHPDoc
     * This method scans the resource class's PHPDoc for a @mixin tag
     * and extracts the first type name found within it
     *
     * @param resourceClass The factory class to inspect
     * @return The model name if found, or null if not found
     */
    public @Nullable static String resolveModelNameFromPhpDocMixinTag(PhpClass resourceClass) {
        PhpDocComment doc = resourceClass.getDocComment();
        if (doc == null) {
            return null;
        }

        PhpDocTag[] tags = doc.getTagElementsByName("@mixin");

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
        return EloquentUtils.getFields(eloquentModelFQN, false, project);
    }

    /**
     * Finds the first simple type name inside a @resource tag
     *
     * @param tag The @mixin tag to inspect
     */
    private @Nullable static String extractFirstTypeName(PhpDocTag tag) {
        for (PsiElement child : tag.getChildren()) {
            if (child instanceof PhpDocTypeImpl phpDocType) {
                return phpDocType.getName();
            }
        }
        return null;
    }
}

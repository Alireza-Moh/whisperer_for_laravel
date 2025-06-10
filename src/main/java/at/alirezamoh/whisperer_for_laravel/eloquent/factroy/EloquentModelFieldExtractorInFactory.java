package at.alirezamoh.whisperer_for_laravel.eloquent.factroy;

import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.whisperer_for_laravel.support.utils.EloquentUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Extracts Eloquent model fields from a factory's definition() method or the @extends doc tag
 * This class is used to resolve the fields of an Eloquent model
 * when working with Laravel factories
 */
public class EloquentModelFieldExtractorInFactory {

    public static final String FACTORY_INIT_METHOD = "definition";

    public static final String BASE_FACTORY_CLASS_NAMESPACE = "\\Illuminate\\Database\\Eloquent\\Factories\\Factory";

    private static Project project;

    /**
     * Extracts the fields of an Eloquent model from a factory's definition method
     * or from the @extends tag in the factory's PHPDoc
     *
     * @param psiElement The PsiElement to inspect, typically a factory method
     * @param project    The current project instance
     * @return A list of {@link Field} objects representing the model's fields, or null if not found
     */
    public static @Nullable List<Field> extract(PsiElement psiElement, Project project) {
        EloquentModelFieldExtractorInFactory.project = project;
        MethodImpl method = MethodUtils.resolveMethodImpl(psiElement, 10);
        if (!isDefinitionMethod(method)) {
            return null;
        }

        PhpClass factoryClass = method.getContainingClass();
        if (!isAFactoryClass(factoryClass)) {
            return null;
        }

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
     * Checks if the provided method is the factory's definition method
     * which is typically named "definition" in Laravel factories
     *
     * @param method The MethodImpl to check
     * @return true or false
     */
    private static boolean isDefinitionMethod(@Nullable MethodImpl method) {
        return method != null && Objects.equals(method.getName(), FACTORY_INIT_METHOD);
    }

    /**
     * Checks if the provided PhpClass is a factory class
     * by checking if it extends the base factory class
     *
     * @param phpClass The PhpClass to check
     * @return true or false
     */
    private static boolean isAFactoryClass(@Nullable PhpClass phpClass) {
        if (!(phpClass instanceof PhpClassImpl)) {
            return false;
        }

        PhpClass baseFactory = PhpClassUtils.getClassByFQN(project, BASE_FACTORY_CLASS_NAMESPACE);
        return baseFactory != null
            && PhpClassUtils.isChildOf((PhpClassImpl) phpClass, baseFactory);
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
        return EloquentUtils.getFields(eloquentModelFQN, false, project);
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

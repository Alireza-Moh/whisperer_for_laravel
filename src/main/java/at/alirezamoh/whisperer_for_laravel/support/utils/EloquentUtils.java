package at.alirezamoh.whisperer_for_laravel.support.utils;

import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Field;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.*;

public class EloquentUtils {
    /**
     * A list of supported PHP data types (for field extraction logic)
     */
    private final static List<String> PHP_DATA_TYPES = Arrays.asList(
        "int",
        "float",
        "string",
        "bool",
        "null",
        "Carbon",
        "double",
        "mixed"
    );

    /**
     * Retrieves the attributes for the specified eloquent model by scanning eloquent_models file created by the plugin
     * If {@code withRelations} is {@code true}, relation fields will also be included
     *
     * @param modelName      The name of the model class to look for
     * @param withRelations  Whether to include fields that may represent relations
     * @param project        The current project
     * @return A list of extracted {@link Field} objects.
     */
    public static List<Field> getFields(String modelName, boolean withRelations, Project project) {
        List<Field> fields = new ArrayList<>();

        PsiDirectory pluginVendor = PluginUtils.getPluginVendor(project);

        if (pluginVendor == null) {
            return fields;
        }

        for (PsiFile file : pluginVendor.getFiles()) {
            if (file instanceof PhpFile phpFile) {
                PhpClass modelPhpClass = PhpClassUtils.getPhpClassFromFile(phpFile, modelName);
                if (modelPhpClass != null) {
                    extractFieldsFromClass(modelPhpClass, withRelations, fields);
                }
            }
        }

        return fields;
    }


    /**
     * Extracts fields from a given PhpClass (eloquent model)
     * If {@code withRelations} is {@code true}, all fields are included; otherwise, only fields
     *
     * @param phpClass       The PHP class which represents a eloquent model
     * @param withRelations  Whether to include fields that may represent relations
     * @param fields         A list to which the extracted {@link Field} instances are added
     */
    public static void extractFieldsFromClass(PhpClass phpClass, boolean withRelations, List<Field> fields) {
        for (com.jetbrains.php.lang.psi.elements.Field field : phpClass.getOwnFields()) {
            PsiElement prev = field.getPrevPsiSibling();

            if (withRelations) {
                fields.add(new Field(field.getName()));
            }
            else {
                if (prev instanceof PhpDocTypeImpl phpDocType && PHP_DATA_TYPES.contains(phpDocType.getName())) {
                    fields.add(new Field(field.getName()));
                }
            }
        }
    }
}

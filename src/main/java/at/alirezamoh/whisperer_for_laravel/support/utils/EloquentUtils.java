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

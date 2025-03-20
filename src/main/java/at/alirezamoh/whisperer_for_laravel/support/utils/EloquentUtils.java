package at.alirezamoh.whisperer_for_laravel.support.utils;

import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.whisperer_for_laravel.indexes.ModelFactoryIndex;
import at.alirezamoh.whisperer_for_laravel.indexes.TableIndex;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Retrieves the base Eloquent model class
     *
     * @param project the current project
     * @return the base Eloquent model as a PhpClass or {@code null} if not found
     */
    public static @Nullable PhpClass getEloquentBaseModel(Project project) {
        return PhpIndex.getInstance(project)
            .getClassesByFQN(LaravelPaths.LaravelClasses.Model)
            .stream()
            .findFirst()
            .orElse(null);
    }

    /**
     * Determines whether a given PHP class is an Eloquent model
     * by checking provided class is an instance base eloquent model
     *
     * @param model   The eloquent model
     * @param project the current project
     * @return {@code true} if the provided class is an Eloquent model; {@code false} otherwise
     */
    public static boolean isEloquentModel(PhpClass model, Project project) {
        PhpClass eloquentModel = getEloquentBaseModel(project);
        if (eloquentModel == null) {
            return false;
        }

        if (!(model instanceof PhpClassImpl phpClassImpl)) {
            return false;
        }

        return PhpClassUtils.isChildOf(phpClassImpl, eloquentModel);
    }

    /**
     * Retrieves a list of migration files associated with a given Eloquent model
     * This method searches the project's index for files that contain the specified table name
     *
     * @param project   the current project
     * @param tableName the name of the database table associated with the model
     * @return a list of migration files as PsiFile objects, or {@code null} if none are found
     */
    public static @Nullable List<PsiFile> getMigrationFilesForEloquentModel(Project project, String tableName) {
        Collection<VirtualFile> paths = FileBasedIndex.getInstance().getContainingFiles(
            TableIndex.INDEX_ID,
            tableName,
            GlobalSearchScope.allScope(project)
        );

        if (paths.isEmpty()) {
            return null;
        }

        List<PsiFile> foundedFiles = new ArrayList<>();
        for (VirtualFile path : paths) {
            if (path != null) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(path);

                if (psiFile != null) {
                    foundedFiles.add(psiFile);
                }
            }
        }

        return foundedFiles;
    }

    /**
     * Determines the database table name for a given Eloquent model
     * The method first checks if the model explicitly defines a "table" field.
     * If it does, the field's value is returned as the table name
     * Otherwise the model's name is converted from CamelCase to snake_case and the last segment is pluralized
     * following Laravel's naming conventions
     *
     * @param model the PHP class representing the Eloquent model
     * @return the table name for the model
     */
    public static String getTableName(PhpClass model) {
        String tableName = "";
        com.jetbrains.php.lang.psi.elements.Field tableField = model.findOwnFieldByName("table", false);

        if (tableField != null) {
            tableName = StrUtils.removeQuotes(tableField.getDefaultValuePresentation());
        }
        else {
            String modelNameWithoutExtension = StrUtils.removePhpExtension(model.getName());
            if (StrUtils.isCamelCase(modelNameWithoutExtension)) {
                String[] parts = StrUtils.snake(modelNameWithoutExtension, "_").split("_");

                parts[parts.length - 1] = StringUtil.pluralize(parts[parts.length - 1]);

                tableName = StrUtils.lcFirst(String.join("_", parts));
            }
        }

        return tableName;
    }

    /**
     * Collects factories associated with a given Eloquent model
     *
     * <p>This method retrieves all factory files that belong to the specified model by querying the
     * {@link ModelFactoryIndex}
     *
     * @param project   The current {@link Project}
     * @param modelName  The eloquent model name
     * @param files     A list to store the {@link PsiFile} instances corresponding to the located factory files
     */
    public static void collectFactoriesForModel(Project project, String modelName, List<PsiFile> files) {
        Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(
            ModelFactoryIndex.INDEX_ID,
            modelName,
            GlobalSearchScope.projectScope(project)
        );

        PsiManager psiManager = PsiManager.getInstance(project);
        for (VirtualFile virtualFile : virtualFiles) {
            files.add(psiManager.findFile(virtualFile));
        }
    }
}

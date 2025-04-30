package at.alirezamoh.whisperer_for_laravel.support.codeGeneration;

import at.alirezamoh.whisperer_for_laravel.actions.models.codeGenerationHelperModels.LaravelModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.*;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Method;
import at.alirezamoh.whisperer_for_laravel.indexes.TableIndex;
import at.alirezamoh.whisperer_for_laravel.support.codeGeneration.vistors.MigrationVisitor;
import at.alirezamoh.whisperer_for_laravel.support.utils.*;
import at.alirezamoh.whisperer_for_laravel.support.providers.ModelProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.Parameter;
import org.jetbrains.annotations.Nullable;

import java.util.*;


/**
 * Manages Laravel migration files and generates corresponding model information
 * This class processes migrations to extract table structure, fields, and relationships
 * and generates Laravel models enriched with helper methods and relations
 */
public class MigrationManager {
    public static final String ELOQUENT_BUILDER_NAMESPACE = "\\Illuminate\\Database\\Eloquent\\Builder";

    public static Map<String, Integer> RELATION_METHODS = new HashMap<>() {{
        put("belongsTo", 0);
        put("belongsToMany", 0);
        put("hasMany", 0);
        put("hasManyThrough", 0);
        put("hasOne", 0);
        put("hasOneOrMany", 0);
        put("hasOneOrManyThrough", 0);
        put("hasOneThrough", 0);
        put("morphMany", 0);
        put("morphOne", 0);
        put("morphTo", 0);
        put("morphToMany", 0);
    }};

    private Project project;

    private List<LaravelModel> models = new ArrayList<>();

    private Collection<PsiFile> originalModels = new ArrayList<>();

    private List<Table> tables = new ArrayList<>();

    private final ModelMethodRelationGenerator modelMethodRelationGenerator;

    public MigrationManager(Project project) {
        this.project = project;
        this.modelMethodRelationGenerator = new ModelMethodRelationGenerator(project);

        getAllModels(project);
    }

    /**
     * Processes all migrations, resolves models, and generates Laravel models
     *
     * @return a list of generated Laravel models with fields and relationships
     */
    public List<LaravelModel> visit() {
        searchForMigrations();
        removeDuplicatedTable();

        for (Table table : tables) {
            String tableName = table.name();
            PhpClass modelClass = getModelByTableName(tableName);
            if (modelClass != null) {
                LaravelModel laravelModel = new LaravelModel();
                laravelModel.setNamespaceName(StrUtils.addBackSlashes(modelClass.getNamespaceName(), true, true));
                laravelModel.setModelName(modelClass.getName());
                laravelModel.setTableName(tableName);

                laravelModel.setFields(replaceFieldTypeByFromCasts(table, modelClass));

                modelMethodRelationGenerator.setEloquentModelAsPhpClass(modelClass);
                List<Relation> relations = modelMethodRelationGenerator.createMethodsFromModelRelations();

                laravelModel.setRelations(relations);
                laravelModel.addFields(modelMethodRelationGenerator.getRelationsAsFields());
                createModelMagicMethods(laravelModel);
                addLocalScopeMethods(modelClass, laravelModel);
                addAggregateFields(relations, laravelModel);

                models.add(laravelModel);
            }
        }

        return models;
    }

    /**
     * Retrieves all original laravel models available in the project
     *
     * @param project the current project
     */
    private void getAllModels(Project project) {
        ModelProvider modelProvider = new ModelProvider(project, true);
        this.originalModels = modelProvider.getOriginalModels();
    }

    /**
     * Searches for migration files and extracts tables
     */
    private void searchForMigrations() {
        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
        Set<String> keys = new HashSet<>();

        FileBasedIndex.getInstance().processAllKeys(
            TableIndex.INDEX_ID,
            key -> {
                keys.add(key);
                return true;
            },
            project
        );

        PsiManager psiManager = PsiManager.getInstance(project);
        fileBasedIndex.processFilesContainingAnyKey(
            TableIndex.INDEX_ID,
            keys,
            GlobalSearchScope.allScope(project),
            null,
            null,
            file -> {
                PsiFile psiFile = psiManager.findFile(file);
                if (psiFile != null) {
                    MigrationVisitor migrationVisitor = new MigrationVisitor();
                    psiFile.acceptChildren(migrationVisitor);
                    this.tables.addAll(migrationVisitor.getTables());
                }
                return true;
            }
        );
    }

    /**
     * Removes duplicate tables by merging them and cleans up fields
     */
    private void removeDuplicatedTable() {
        List<Table> mergedTables = new ArrayList<>();

        for (Table currentTable : tables) {
            Optional<Table> existingTableOptional = mergedTables.stream()
                .filter(table -> table.name().equals(currentTable.name()))
                .findFirst();

            if (existingTableOptional.isPresent()) {
                Table existingTable = existingTableOptional.get();

                existingTable.fields().addAll(currentTable.fields());
            } else {
                mergedTables.add(new Table(currentTable.name(), currentTable.navigationElement(), currentTable.fields()));
            }
        }

        tables = mergedTables;

        removeDuplicatedFields();
        dropFields();
        renameFields();
    }

    /**
     * Removes duplicate fields from tables to ensure each field is unique
     */
    private void removeDuplicatedFields() {
        for (Table currentTable : tables) {
            List<Field> fields = currentTable.fields();
            Set<String> uniqueFieldIdentifiers = new HashSet<>();
            List<Field> removedFields = new ArrayList<>();

            for (Field currentField : fields) {
                String identifier = currentField.getName() + "-" + currentField.getType();

                if (uniqueFieldIdentifiers.contains(identifier)) {
                    removedFields.add(currentField);
                } else {
                    uniqueFieldIdentifiers.add(identifier);
                }
            }

            fields.removeAll(removedFields);
        }
    }

    /**
     * Removes fields marked as dropped from the table
     */
    private void dropFields() {
        for (Table currentTable : tables) {
            List<Field> droppedFields = new ArrayList<>();
            for (Field currentField : currentTable.fields()) {
                if (currentField.isDrop()) {
                    List<Field> foundFields = currentTable.fields().stream()
                        .filter(field -> currentField.getName().equals(field.getName()) && !field.isDrop() && !field.isRename())
                        .toList();

                    droppedFields.addAll(foundFields);
                    droppedFields.add(currentField);
                }
            }

            currentTable.fields().removeAll(droppedFields);
        }
    }

    /**
     * Renames fields marked for renaming in the table
     */
    private void renameFields() {
        for (Table currentTable : tables) {
            List<Field> droppedFields = new ArrayList<>();
            for (Field currentField : currentTable.fields()) {
                if (currentField.isRename() && currentField.getRenameField() != null) {
                    Field foundField = currentTable.fields().stream()
                        .filter(field -> currentField.getRenameField().oldName().equals(field.getName()))
                        .findFirst()
                        .orElse(null);

                    if (foundField != null) {
                        foundField.setName(currentField.getRenameField().newName());
                        droppedFields.add(currentField);
                    }
                }
            }

            currentTable.fields().removeAll(droppedFields);
        }
    }

    /**
     * Creates helper methods (like where conditions) for the Laravel model based on its fields
     *
     * @param laravelModel the model
     */
    private void createModelMagicMethods(LaravelModel laravelModel) {
        for (Field field : laravelModel.getFields()) {
            if (!field.getName().isEmpty()) {
                Method method = new Method(
                    "where" + StrUtils.ucFirst(
                        StrUtils.camel(field.getName(), '_')
                    )
                );
                method.setReturnType(ELOQUENT_BUILDER_NAMESPACE + "|" + laravelModel.getModelName());
                method.addParameter("value", "mixed", "");
                laravelModel.addMethod(method);
            }
        }
    }

    /**
     * Finds a PhpClass model corresponding to a table name
     *
     * @param tableName the table
     * @return the matching PhpClass or null if not found
     */
    private @Nullable PhpClass getModelByTableName(String tableName) {
        for (PsiFile modelFile : originalModels) {
            if (!(modelFile instanceof PhpFile phpFile)) {
                return null;
            }

            for (PhpClass phpClass : PhpClassUtils.getPhpClassesFromFile(phpFile)) {
                if (EloquentUtils.getTableName(phpClass).equals(tableName)) {
                    return phpClass;
                }
            }
        }

        return null;
    }

    private void addLocalScopeMethods(PhpClass model, LaravelModel laravelModel) {
        for (com.jetbrains.php.lang.psi.elements.Method method : model.getOwnMethods()) {
            String methodName = method.getName();
            if (methodName.startsWith("scope")) {
                Method localScopeMethod = new Method(
                    StrUtils.lcFirst(methodName.replace("scope", ""))
                );
                localScopeMethod.setReturnType(
                    "\\Illuminate\\Database\\Eloquent\\Builder"
                );
                for (Parameter param : method.getParameters()) {
                    localScopeMethod.addParameter(param.getName(), param.getType().toString(), "");
                }

                laravelModel.addMethod(localScopeMethod);
            }
        }
    }

    /**
     * Replaces the field types in the table with the types defined in the model's casts
     *
     * @param table      the table to process
     * @param modelClass the model class to check for casts
     * @return a list of fields with updated types
     */
    private List<Field> replaceFieldTypeByFromCasts(Table table, PhpClass modelClass) {
        List<Field> fields = table.fields();
        Map<String, String> casts = ModelCastsResolver.resolveCasts(modelClass);
        if (!casts.isEmpty()) {
            for (Field field : fields) {
                String castType = casts.get(field.getName());
                if (castType != null) {
                    field.setType(castType);
                }
            }
        }
        return fields;
    }

    /**
     * Adds aggregate fields to the model based on the relations
     *
     * @param relations    the list of relations in the model
     * @param laravelModel the Laravel model to add fields to
     */
    private void addAggregateFields(List<Relation> relations, LaravelModel laravelModel) {
        for (Relation relation : relations) {
            if (relation.isArrayOrCollection()) {
                String aggregateName = StrUtils.snake(relation.getName(), "_") + "_count";
                laravelModel.addReadProperty(new ReadProperty(aggregateName, "int"));
            }
        }
    }
}

package at.alirezamoh.whisperer_for_laravel.support.codeGeneration;

import at.alirezamoh.whisperer_for_laravel.actions.models.codeGenerationHelperModels.LaravelModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Method;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Relation;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Table;
import at.alirezamoh.whisperer_for_laravel.indexes.TableIndex;
import at.alirezamoh.whisperer_for_laravel.support.codeGeneration.vistors.MigrationVisitor;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.LaravelPaths;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.providers.ModelProvider;
import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.*;
import org.jetbrains.annotations.NotNull;
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

    public MigrationManager(Project project) {
        this.project = project;

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
                laravelModel.setNamespaceName(StrUtil.addBackSlashes(modelClass.getNamespaceName(), true, true));
                laravelModel.setModelName(modelClass.getName());
                laravelModel.setTableName(tableName);
                laravelModel.setFields(table.fields());

                List<Relation> relations = new ArrayList<>();
                for (com.jetbrains.php.lang.psi.elements.Method method : resolveAllRelations(modelClass, project)) {
                    Map.Entry<String, PhpClass> methodNameAndRelatedModel = findRelationships(method);
                    if (methodNameAndRelatedModel != null) {
                        relations.add(
                            new Relation(
                                method.getName(),
                                methodNameAndRelatedModel.getKey(),
                                "\\Illuminate\\Database\\Eloquent\\Relations\\"
                                    + StrUtil.capitalizeFirstLetter(methodNameAndRelatedModel.getKey())
                                    + "|"
                                    + methodNameAndRelatedModel.getValue().getName()
                            )
                        );
                        laravelModel.addField(
                            new Field(methodNameAndRelatedModel.getValue().getName(), method.getName(), false)
                        );
                    }
                }
                laravelModel.setRelations(relations);
                createLaravelModelHelperMethods(laravelModel);
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
        Collection<VirtualFile> files = new ArrayList<>();

        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();

        Collection<String> tables = fileBasedIndex.getAllKeys(TableIndex.INDEX_ID, project);

        for (String table : tables) {
            files.addAll(
                fileBasedIndex.getContainingFiles(
                    TableIndex.INDEX_ID,
                    table,
                    GlobalSearchScope.allScope(project)
                )
            );
        }

        for (VirtualFile file : files) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

            if (psiFile != null) {
                MigrationVisitor migrationVisitor = new MigrationVisitor();
                psiFile.acceptChildren(migrationVisitor);
                this.tables.addAll(migrationVisitor.getTables());
            }
        }
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
    private void createLaravelModelHelperMethods(LaravelModel laravelModel) {
        for (Field field : laravelModel.getFields()) {
            if (!field.getName().isEmpty()) {
                Method method = new Method(
                    "where" + StrUtil.capitalizeFirstLetter(
                        StrUtil.camel(field.getName())
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
        final PhpClass[] finalModel = {null};
        for (PsiFile model : originalModels) {
            model.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof PhpClass modelClass) {
                        String finalModelName = "";
                        String modelNameWithoutExtension = StrUtil.removeExtension(modelClass.getName());
                        if (StrUtil.isCamelCase(modelNameWithoutExtension)) {
                            String[] parts = StrUtil.snake(modelNameWithoutExtension).split("_");
                            String lastWord = parts[parts.length - 1];

                            parts[parts.length - 1] = StrUtil.plural(lastWord);
                            finalModelName = String.join("_", parts);
                        }

                        if (decapitalize(finalModelName).equals(tableName)) {
                            finalModel[0] = modelClass;
                        }
                    }
                    super.visitElement(element);
                }
            });
        }


        return finalModel[0];
    }

    /**
     * Decapitalizes the first character of a string
     *
     * @param str the input string.
     * @return the decapitalized string.
     */
    private String decapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * Finds the related model relationships.
     *
     * @param foundedMethod the relationship method defined in the eloquent model
     * @return an entry containing the method name and related PhpClass, or null if no relationship is found
     */
    public @Nullable Map.Entry<String, PhpClass> findRelationships(com.jetbrains.php.lang.psi.elements.Method foundedMethod) {
        return Arrays.stream(foundedMethod.getChildren())
            .filter(element -> element instanceof GroupStatementImpl)
            .flatMap(groupStatement -> Arrays.stream(groupStatement.getChildren()))
            .filter(child -> child instanceof PhpReturnImpl)
            .flatMap(phpReturn -> Arrays.stream(phpReturn.getChildren()))
            .filter(child2 -> child2 instanceof MethodReferenceImpl)
            .map(child2 -> (MethodReferenceImpl) child2)
            .map(methodReference -> {
                PhpClass relatedModel = findRelatedModelFromMethod(foundedMethod);
                if (relatedModel != null) {
                    return new AbstractMap.SimpleEntry<>(methodReference.getName(), relatedModel);
                }
                return null;
            })
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    /**
     * Resolves all relationships defined in the given model class
     *
     * @param model   the model to analyze for relationships
     * @param project the project
     * @return a list of relationships in the given model class
     */
    public List<com.jetbrains.php.lang.psi.elements.Method> resolveAllRelations(PhpClass model, Project project) {
        List<com.jetbrains.php.lang.psi.elements.Method> relations = new ArrayList<>();

        for (com.jetbrains.php.lang.psi.elements.Method method : model.getOwnMethods()) {
            if (isRelationshipMethod(method, project)) {
                relations.add(method);
            }
        }

        return relations;
    }

    /**
     * Determines whether the given method is a Laravel relationship method
     *
     * @param method  the method to check
     * @param project the project
     * @return true or false
     */
    public boolean isRelationshipMethod(com.jetbrains.php.lang.psi.elements.Method method, Project project) {
        return Arrays.stream(method.getChildren())
            .filter(element -> element instanceof GroupStatementImpl)
            .flatMap(element -> Arrays.stream(element.getChildren()))
            .filter(child -> child instanceof PhpReturnImpl)
            .flatMap(child -> Arrays.stream(child.getChildren()))
            .filter(child2 -> child2 instanceof MethodReferenceImpl)
            .map(child2 -> (MethodReferenceImpl) child2)
            .anyMatch(methodReference -> {
                List<PhpClassImpl> classes = MethodUtils.resolveMethodClasses(methodReference, project);
                PhpClass relationClass = ClassUtils.getClassByFQN(project, LaravelPaths.LaravelClasses.Model);

                return RELATION_METHODS.containsKey(methodReference.getName())
                    && relationClass != null
                    && classes.stream().anyMatch(clazz -> ClassUtils.isChildOf(clazz, relationClass));
            });
    }

    /**
     * Resolves the related model referenced in a relationship method
     *
     * @param foundedMethod the relationship method to analyze
     * @return the PhpClass of the related model, or null if no related model is found
     */
    public @Nullable PhpClass findRelatedModelFromMethod(com.jetbrains.php.lang.psi.elements.Method foundedMethod) {
        return Arrays.stream(foundedMethod.getChildren())
            .filter(element -> element instanceof GroupStatementImpl)
            .flatMap(groupStatement -> Arrays.stream(groupStatement.getChildren()))
            .filter(child -> child instanceof PhpReturnImpl)
            .flatMap(phpReturn -> Arrays.stream(phpReturn.getChildren()))
            .filter(child2 -> child2 instanceof MethodReferenceImpl)
            .map(child2 -> (MethodReferenceImpl) child2)
            .map(methodReference -> methodReference.getParameter(0))
            .filter(parameter -> parameter instanceof ClassConstantReferenceImpl)
            .map(parameter -> ((ClassConstantReferenceImpl) parameter).getClassReference())
            .filter(phpExpression -> phpExpression instanceof ClassReferenceImpl)
            .map(phpExpression -> ((ClassReferenceImpl) phpExpression).resolve())
            .filter(resolvedClass -> resolvedClass instanceof PhpClass)
            .map(resolvedClass -> (PhpClass) resolvedClass)
            .findFirst()
            .orElse(null);
    }
}

package at.alirezamoh.idea_whisperer_for_laravel.support.codeGeneration;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.codeGenerationHelperModels.LaravelModel;
import at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables.Method;
import at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables.Table;
import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.codeGeneration.vistors.MigrationVisitor;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.providers.ModelProvider;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MigrationManager {
    static final String ELOQUENT_BUILDER_NAMESPACE = "\\Illuminate\\Database\\Eloquent\\Builder";

    private Project project;

    private SettingsState projectSettingsState;

    private List<LaravelModel> models = new ArrayList<>();

    private Collection<PsiFile> originalModels = new ArrayList<>();

    private List<Table> tables = new ArrayList<>();

    public MigrationManager(Project project) {
        this.project = project;
        this.projectSettingsState = SettingsState.getInstance(project);

        getAllModels(project);
    }

    public List<LaravelModel> visit() {
        searchForMigrations();
        removeDuplicatedTable();

        for (Table table : tables) {
            String tableName = table.name();
            String modelName = getModelByTableName(tableName);
            if (modelName != null) {
                LaravelModel laravelModel = new LaravelModel();
                laravelModel.setModelName(modelName);
                laravelModel.setTableName(tableName);
                laravelModel.setFields(table.fields());
                createLaravelModelHelperMethods(laravelModel);
                models.add(laravelModel);
            }
        }

        return models;
    }

    public List<Table> getTables() {
        searchForMigrations();
        removeDuplicatedTable();

        return tables;
    }

    private void getAllModels(Project project) {
        ModelProvider modelProvider = new ModelProvider(project, projectSettingsState, true);
        this.originalModels = modelProvider.getOriginalModels();
    }

    private void searchForMigrations() {
        Collection<PsiFile> migrations = DirectoryPsiUtil.getFilesRecursively(project, ProjectDefaultPaths.MIGRATION_PATH);

        if (projectSettingsState.isModuleApplication()) {
            searchForMigrationsInModules(migrations);
        }

        for (PsiFile migration : migrations) {
            MigrationVisitor migrationVisitor = new MigrationVisitor();
            migration.acceptChildren(migrationVisitor);
            tables.addAll(migrationVisitor.getTables());
        }
    }

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
                mergedTables.add(new Table(currentTable.name(), currentTable.fields()));
            }
        }

        tables = mergedTables;

        removeDuplicatedFields();
        dropFields();
        renameFields();
    }

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

    private void searchForMigrationsInModules(Collection<PsiFile> migrations) {
        String moduleRootPath = projectSettingsState.replaceAndSlashes(projectSettingsState.getModuleRootDirectoryPath());
        PsiDirectory rootDir = DirectoryPsiUtil.getDirectory(project, moduleRootPath);

        if (rootDir != null) {
            for (PsiDirectory module : rootDir.getSubdirectories()) {
                migrations.addAll(
                    DirectoryPsiUtil.getFilesRecursively(project, moduleRootPath + module.getName() + ProjectDefaultPaths.MIGRATION_PATH)
                );
            }
        }
    }

    private @Nullable String getModelByTableName(String tableName) {
        for (PsiFile model : originalModels) {
            String finalModelName = "";
            String modelNameWithoutExtension = StrUtil.removeExtension(model.getName());
            if (StrUtil.isCamelCase(modelNameWithoutExtension)) {
                String[] parts = StrUtil.snake(modelNameWithoutExtension).split("_");
                String lastWord = parts[parts.length - 1];

                parts[parts.length - 1] = StrUtil.plural(lastWord);
                finalModelName = String.join("_", parts);
            }

            if (decapitalize(finalModelName).equals(tableName)) {
                return modelNameWithoutExtension;
            }
        }
        return null;
    }

    private String decapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
}

package at.alirezamoh.idea_whisperer_for_laravel.support.codeGeneration;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.codeGenerationHelperModels.LaravelModel;
import at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables.Method;
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

    public MigrationManager(Project project) {
        this.project = project;
        this.projectSettingsState = SettingsState.getInstance(project);

        getAllModels(project);
    }

/*    public List<LaravelModel> visit() {
        Collection<PsiFile> migrations = DirectoryPsiUtil.getFilesRecursively(project, ProjectDefaultPaths.MIGRATION_PATH);

        if (projectSettingsState.isModuleApplication()) {
            searchForMigrationsInModules(migrations);
        }

        for (PsiFile migration : migrations) {
            MigrationVisitor migrationVisitor = new MigrationVisitor();
            migration.acceptChildren(migrationVisitor);

            Map<String, List<Field>> tables = migrationVisitor.getTables();
            for (Map.Entry<String, List<Field>> entry : tables.entrySet()) {
                String tableName = entry.getKey();

                String modelName = getModelByTableName(tableName);
                if (modelName != null) {
                    LaravelModel laravelModel = new LaravelModel();
                    laravelModel.setModelName(modelName);
                    laravelModel.setTableName(tableName);
                    laravelModel.setFields(entry.getValue());

                    createLaravelModelHelperMethods(laravelModel);

                    models.add(laravelModel);
                }
            }
        }

        return models;
    }*/

    public List<LaravelModel> visit() {
        Collection<PsiFile> migrations = DirectoryPsiUtil.getFilesRecursively(project, ProjectDefaultPaths.MIGRATION_PATH);

        if (projectSettingsState.isModuleApplication()) {
            searchForMigrationsInModules(migrations);
        }

        Map<String, List<Field>> tableFinalState = new HashMap<>();

        for (PsiFile migration : migrations) {
            MigrationVisitor migrationVisitor = new MigrationVisitor();
            migration.acceptChildren(migrationVisitor);

            // Merge migration data into the final table state
            Map<String, List<Field>> tablesInMigration = migrationVisitor.getTables();
            for (String tableName : tablesInMigration.keySet()) {
                List<Field> currentFields = tableFinalState.getOrDefault(tableName, new ArrayList<>());
                mergeTableFields(currentFields, tablesInMigration.get(tableName));
                tableFinalState.put(tableName, currentFields);
            }
        }

        // Use the final state for model generation
        for (Map.Entry<String, List<Field>> entry : tableFinalState.entrySet()) {
            String tableName = entry.getKey();
            String modelName = getModelByTableName(tableName);
            if (modelName != null) {
                LaravelModel laravelModel = new LaravelModel();
                laravelModel.setModelName(modelName);
                laravelModel.setTableName(tableName);
                laravelModel.setFields(entry.getValue());
                createLaravelModelHelperMethods(laravelModel);
                models.add(laravelModel);
            }
        }

        return models;
    }

    private void mergeTableFields(List<Field> currentFields, List<Field> newFields) {
        // Remove fields marked for dropping
        currentFields.removeIf(existingField -> newFields.stream()
            .anyMatch(newField -> existingField.getName().equals(newField.getName()) && newField.isDrop()));

        // Add new or modified fields
        for (Field newField : newFields) {
            if (!newField.isDrop() && currentFields.stream().noneMatch(field -> field.getName().equals(newField.getName()))) {
                currentFields.add(newField);
            }
        }
    }

    private static void createLaravelModelHelperMethods(LaravelModel laravelModel) {
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

    private void getAllModels(Project project) {
        ModelProvider modelProvider = new ModelProvider(project, projectSettingsState, true);
        this.originalModels = modelProvider.getOriginalModels();
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

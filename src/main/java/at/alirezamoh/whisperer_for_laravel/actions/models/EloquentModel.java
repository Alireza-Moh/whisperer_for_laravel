package at.alirezamoh.whisperer_for_laravel.actions.models;

import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;

import java.util.List;

public class EloquentModel extends BaseModel {
    private List<Field> fields;

    private String tableName;

    private DbFactoryModel dbFactoryModel;

    /**
     * @param name                      The name of the eloquent model
     * @param unformattedModuleFullPath The unformatted full path to the module
     * @param formattedModuleFullPath   The formatted full path to the module
     */
    public EloquentModel(
        SettingsState settingsState,
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath,
        List<Field> fields
    ) {
        super(
            settingsState,
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.ELOQUENT_MODEL_PATH,
            "",
            ".php",
            "Models"
        );

        this.fields = fields;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public void setWithoutModuleSrc() {
        this.withoutModuleSrcPath = false;
    }

    public void setDbFactoryModel(DbFactoryModel dbFactoryModel) {
        this.dbFactoryModel = dbFactoryModel;
    }

    public DbFactoryModel getDbFactoryModel() {
        return dbFactoryModel;
    }
}

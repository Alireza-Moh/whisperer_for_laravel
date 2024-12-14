package at.alirezamoh.idea_whisperer_for_laravel.actions.models;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a middleware class
 */
public class MigrationModel extends BaseModel {
    private String tableName;

    private boolean createTable;

    private boolean hasFields;

    private boolean anonymous;

    private List<Field> fields;

    /**
     * @param name                      The name of the migration class name
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     */
    public MigrationModel(
        SettingsState settingsState,
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath,
        String tableName,
        boolean createTable,
        boolean hasFields,
        boolean anonymous
    )
    {
        super(
            settingsState,
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.MIGRATION_PATH,
            "",
            ".php",
            ""
        );

        this.tableName = tableName;
        this.createTable = createTable;
        this.hasFields = hasFields;
        this.anonymous = anonymous;
        this.fields = new ArrayList<>();

        if (unformattedModuleFullPath.equals("/app") || unformattedModuleFullPath.isEmpty()) {
            this.formattedModuleFullPath = "";
            initDestination();
            initNamespace("");
            initFilePath();
        }
        else {
            initDestination();
            initNamespace("");
            initFilePath();
        }

        modifyFileName();
    }

    public String getTableName() {
        return tableName;
    }

    public boolean isCreateTable() {
        return createTable;
    }

    public boolean isHasFields() {
        return hasFields;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    private void modifyFileName() {
        filePath = filePath.replace(getName() + extension, getRealFileName());
    }

    private String getRealFileName() {
        return StrUtil.getCurrentDate()
            + "_"
            + StrUtil.generateRandomId()
            + StrUtil.snake(getName())
            + extension;
    }

    @Override
    public void setWithoutModuleSrc() {
        this.withoutModuleSrcPath = true;
    }
}

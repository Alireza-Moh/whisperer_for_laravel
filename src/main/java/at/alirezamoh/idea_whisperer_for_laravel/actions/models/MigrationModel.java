package at.alirezamoh.idea_whisperer_for_laravel.actions.models;

import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Model representing a middleware class
 */
public class MigrationModel extends BaseModel {
    private String tableName;

    private boolean createTable;

    private boolean hasFields;

    private boolean anonymous;

    /**
     * @param name                      The name of the migration class name
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     * @param moduleSrcPath             The module src path
     */
    public MigrationModel(
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath,
        String moduleSrcPath,
        String tableName,
        boolean createTable,
        boolean hasFields,
        boolean anonymous
    )
    {
        super(
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.MIGRATION_PATH,
            "",
            ".php",
            "",
            moduleSrcPath
        );

        this.tableName = tableName;
        this.createTable = createTable;
        this.hasFields = hasFields;
        this.anonymous = anonymous;

        if (unformattedModuleFullPath.equals("/app") || unformattedModuleFullPath.isEmpty()) {
            this.formattedModuleFullPath = "";
            initDestination("", ProjectDefaultPaths.MIGRATION_PATH, "");
            initNamespace("");
            initFileName();
        }
        else {
            initDestination(unformattedModuleFullPath, ProjectDefaultPaths.MIGRATION_PATH, "");
            initNamespace("");
            initFileName();
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

    private void modifyFileName() {
        filePath = filePath.replace(getName() + extension, getRealFileName());
    }

    private String getRealFileName() {
        return getCurrentDate()
            + "_"
            + generateRandomId()
            + StrUtil.snake(getName())
            + extension;
    }

    private String getCurrentDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");

        return currentDate.format(formatter);
    }

    private String generateRandomId() {
        Random random = new Random();

        return String.format("%06d_", random.nextInt(1000000));
    }
}

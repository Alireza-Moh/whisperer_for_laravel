package at.alirezamoh.whisperer_for_laravel.actions.models;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;

/**
 * Model representing a laravel DB FACTORY
 */
public class DbFactoryModel extends BaseModel {
    private EloquentModel model;

    private String modelNamespace;

    /**
     * @param name                      The name of the factory
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     */
    public DbFactoryModel(
        SettingsState settingsState,
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath,
        String modelNamespace
    )
    {
        super(
            settingsState,
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.DB_FACTORY_PATH,
            "Factory",
            ".php",
            "Database\\Factories"
        );

        this.modelNamespace = modelNamespace;

        if (unformattedModuleFullPath.equals("app") || unformattedModuleFullPath.isEmpty()) {
            this.unformattedModuleFullPath = "";
            this.formattedModuleFullPath = "";
            initDestination();
            initNamespace("Database\\Factories");
            initFilePath();
        }
        else {
            initDestination();
            initNamespace("");
            initFilePath();
        }
    }

    @Override
    public void setWithoutModuleSrc() {
        this.withoutModuleSrcPath = false;
    }

    public EloquentModel getModel() {
        return model;
    }

    public void setModel(EloquentModel model) {
        this.model = model;
    }

    public String getModelNamespace() {
        return modelNamespace;
    }
}

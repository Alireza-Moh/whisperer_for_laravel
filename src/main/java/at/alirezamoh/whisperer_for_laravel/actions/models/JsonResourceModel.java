package at.alirezamoh.whisperer_for_laravel.actions.models;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;

/**
 * Model representing a laravel JSON RESOURCE
 */
public class JsonResourceModel extends BaseModel {
    private EloquentModel model;

    private String modelNamespace;

    private boolean includeRelations;

    /**
     * @param name                      The name of the json resource
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     */
    public JsonResourceModel(
        SettingsState settingsState,
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath,
        String modelNamespace,
        boolean includeRelations
    )
    {
        super(
            settingsState,
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.JSON_RESOURCE_PATH,
            "Resource",
            ".php",
            "Http\\Resources"
        );

        this.modelNamespace = modelNamespace;
        this.includeRelations = includeRelations;
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

    public boolean isIncludeRelations() {
        return includeRelations;
    }
}

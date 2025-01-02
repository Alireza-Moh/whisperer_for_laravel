package at.alirezamoh.whisperer_for_laravel.actions.models;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;

/**
 * Model representing a laravel JSON RESOURCE
 */
public class JsonResourceCollectionModel extends BaseModel {
    /**
     * @param name                      The name of the json resource
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     */
    public JsonResourceCollectionModel(
        SettingsState settingsState,
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath
    )
    {
        super(
            settingsState,
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.JSON_RESOURCE_PATH,
            "Collection",
            ".php",
            "Http\\Resources"
        );
    }

    @Override
    public void setWithoutModuleSrc() {
        this.withoutModuleSrcPath = false;
    }
}

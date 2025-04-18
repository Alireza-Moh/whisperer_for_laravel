package at.alirezamoh.whisperer_for_laravel.actions.models;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;

/**
 * Model representing a laravel config file
 */
public class ConfigFileModel extends BaseModel {
    /**
     * @param name                      The name of the view file
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     */
    public ConfigFileModel(
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
            ProjectDefaultPaths.CONFIG_PATH,
            "",
            ".php",
            ""
        );

        if (unformattedModuleFullPath.equals("/app")) {
            initDestination();
            initNamespace("");
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
        this.withoutModuleSrcPath = true;
    }
}

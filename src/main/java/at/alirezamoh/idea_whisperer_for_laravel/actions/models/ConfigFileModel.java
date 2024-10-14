package at.alirezamoh.idea_whisperer_for_laravel.actions.models;

import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;

/**
 * Model representing a laravel config file
 */
public class ConfigFileModel extends BaseModel {
    /**
     * @param name                      The name of the view file
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     * @param moduleSrcPath             The module src path
     */
    public ConfigFileModel(
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath,
        String moduleSrcPath
    )
    {
        super(
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.CONFIG_PATH,
            "",
            ".php",
            "",
            moduleSrcPath
        );

        if (unformattedModuleFullPath.equals("/app")) {
            initDestination("", ProjectDefaultPaths.CONFIG_PATH, "");
            initNamespace("");
            initFileName();
        }
        else {
            initDestination(unformattedModuleFullPath, ProjectDefaultPaths.CONFIG_PATH, "");
            initNamespace("");
            initFileName();
        }
    }
}

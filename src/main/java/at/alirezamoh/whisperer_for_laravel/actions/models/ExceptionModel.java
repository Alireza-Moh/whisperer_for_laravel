package at.alirezamoh.whisperer_for_laravel.actions.models;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;

/**
 * Model representing an PHP/Laravel exception
 */
public class ExceptionModel extends BaseModel {
    /**
     * @param name                      The name of the exception class
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     */
    public ExceptionModel(
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
            ProjectDefaultPaths.EXCEPTION_PATH,
            "Exception",
            ".php",
            "Exceptions"
        );
    }

    @Override
    public void setWithoutModuleSrc() {
        this.withoutModuleSrcPath = false;
    }
}

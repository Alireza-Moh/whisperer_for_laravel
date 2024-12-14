package at.alirezamoh.idea_whisperer_for_laravel.actions.models;

import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;

/**
 * Model representing a blade view file
 */
public class ViewMailableModel extends BaseModel {
    /**
     * @param name                      The name of the view mailable file
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     */
    public ViewMailableModel(
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
            ProjectDefaultPaths.MAILABLE_VIEW_PATH,
            "",
            ".blade.php",
            ""
        );
    }

    @Override
    public void setWithoutModuleSrc() {
        this.withoutModuleSrcPath = true;
    }
}

package at.alirezamoh.idea_whisperer_for_laravel.actions.models;

import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;

/**
 * Model representing a Blade component view
 */
public class BladeComponentViewModel extends BaseModel {
    /**
     * @param name                      The name of the view file
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     */
    public BladeComponentViewModel(
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
            ProjectDefaultPaths.BLADE_COMPONENT_VIEW_PATH,
            "",
            ".blade.php",
            ""
        );

        this.name = StrUtil.snake(getName(), "-");

        initFilePath();
    }

    @Override
    public void setWithoutModuleSrc() {
        this.withoutModuleSrcPath = true;
    }
}

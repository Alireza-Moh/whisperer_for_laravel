package at.alirezamoh.whisperer_for_laravel.actions.models;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;

import java.util.Arrays;

/**
 * Model representing a livewire blade component
 */
public class LivewireComponentViewModel extends BaseModel {
    public String viewNameForParent;

    /**
     * @param name                      The name of the view file
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     */
    public LivewireComponentViewModel(
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
            ProjectDefaultPaths.LIVEWIRE_COMPONENT_VIEW_PATH,
            "",
            ".blade.php",
            ""
        );

        String[] names = getNameAsArray();
        names[names.length - 1] = StrUtils.snake(getName(), "-");
        this.viewNameForParent = String.join(".", names);

        this.name = StrUtils.snake(getName(), "-");

        initFilePath();
    }

    @Override
    public void setWithoutModuleSrc() {
        this.withoutModuleSrcPath = true;
    }
}

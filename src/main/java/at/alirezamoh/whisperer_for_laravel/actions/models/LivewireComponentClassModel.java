package at.alirezamoh.whisperer_for_laravel.actions.models;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;

/**
 * Model representing a livewire component class
 */
public class LivewireComponentClassModel extends BaseModel {
    private String viewName;
    private boolean inline;

    /**
     * @param name                      The name of the component class
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     */
    public LivewireComponentClassModel(
        SettingsState settingsState,
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath,
        boolean inline
    )
    {
        super(
            settingsState,
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.LIVEWIRE_COMPONENT_CLASS_PATH,
            "",
            ".php",
            "Livewire"
        );

        this.inline = inline;
    }

    @Override
    public void setWithoutModuleSrc() {
        this.withoutModuleSrcPath = false;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        if (viewName.startsWith(".")) {
            viewName = viewName.substring(1);
        }

        this.viewName = "livewire." + viewName;
    }

    public boolean isInline() {
        return inline;
    }
}

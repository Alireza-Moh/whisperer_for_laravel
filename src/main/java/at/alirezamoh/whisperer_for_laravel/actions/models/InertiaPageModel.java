package at.alirezamoh.whisperer_for_laravel.actions.models;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;

/**
 * Model representing an inertia page
 */
public class InertiaPageModel extends BaseModel {
    private String pageName;

    private boolean withOptionsApi;

    private boolean vue;

    /**
     * @param name                      The name of the inertia page
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     */
    public InertiaPageModel(
        SettingsState settingsState,
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath,
        String defaultDestination,
        boolean withOptionsApi,
        String pageType
    )
    {
        super(
            settingsState,
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            defaultDestination,
            "",
            pageType,
            ""
        );

        this.pageName = getName();
        this.withOptionsApi = withOptionsApi;
        this.vue = pageType.equals(".vue");
    }

    @Override
    public void setWithoutModuleSrc() {
        this.withoutModuleSrcPath = true;
    }

    public String getPageName() {
        return pageName;
    }

    public boolean isWithOptionsApi() {
        return withOptionsApi;
    }

    public boolean isVue() {
        return vue;
    }
}

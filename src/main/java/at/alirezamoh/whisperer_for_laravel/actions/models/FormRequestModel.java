package at.alirezamoh.whisperer_for_laravel.actions.models;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;

/**
 * Model representing a laravel form request
 */
public class FormRequestModel extends BaseModel {
    private EloquentModel model;

    private String modelNamespace;

    private boolean authorize;

    /**
     * @param name                      The name of the form request
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     */
    public FormRequestModel(
        SettingsState settingsState,
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath,
        String modelNamespace,
        boolean authorize
    )
    {
        super(
            settingsState,
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.FORM_REQUEST_PATH,
            "Request",
            ".php",
            "Http\\Requests"
        );

        this.modelNamespace = modelNamespace;
        this.authorize = authorize;
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

    public boolean isAuthorize() {
        return authorize;
    }
}

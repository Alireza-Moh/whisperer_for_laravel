package at.alirezamoh.idea_whisperer_for_laravel.actions.models;

import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import org.jetbrains.annotations.Nullable;

/**
 * Model representing a Blade component class
 */
public class PolicyModel extends BaseModel {
    private String eloquentModelPath;

    private String eloquentModelName;

    private String eloquentModelNameVariable;

    private boolean hasModel;

    /**
     * @param name                      The name of policy class
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     */
    public PolicyModel(
        SettingsState settingsState,
        String name,
        @Nullable String eloquentModelPath,
        String unformattedModuleFullPath,
        String formattedModuleFullPath
    )
    {
        super(
            settingsState,
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.POLICY_PATH,
            "Policy",
            ".php",
            "Policies"
        );

        if (eloquentModelPath != null && !eloquentModelPath.isEmpty()) {
            this.eloquentModelPath = eloquentModelPath;
            this.eloquentModelName = eloquentModelPath.substring(eloquentModelPath.lastIndexOf("\\") + 1);
            this.eloquentModelNameVariable = this.getModelVariableName(this.eloquentModelName);
            this.hasModel = true;
        }
    }

    public String getEloquentModelPath() {
        return eloquentModelPath;
    }

    public String getEloquentModelName() {
        return eloquentModelName;
    }

    public String getEloquentModelNameVariable() {
        return eloquentModelNameVariable;
    }

    public boolean isHasModel() {
        return hasModel;
    }

    @Override
    public void setWithoutModuleSrc() {
        this.withoutModuleSrcPath = false;
    }
}

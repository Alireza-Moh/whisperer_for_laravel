package at.alirezamoh.whisperer_for_laravel.actions.models;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;

/**
 * Model representing a laravel job class
 */
public class JobModel extends BaseModel {
    /**
     * @param name                      The name of the job class
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     */
    public JobModel(
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
            ProjectDefaultPaths.JOB_PATH,
            "Job",
            ".php",
            "Jobs"
        );
    }

    @Override
    public void setWithoutModuleSrc() {
        this.withoutModuleSrcPath = false;
    }
}

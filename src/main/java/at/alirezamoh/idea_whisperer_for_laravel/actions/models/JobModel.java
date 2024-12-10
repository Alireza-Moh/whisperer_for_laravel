package at.alirezamoh.idea_whisperer_for_laravel.actions.models;

import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import com.intellij.openapi.project.Project;

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
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath
    )
    {
        super(
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.JOB_PATH,
            "Job",
            ".php",
            "Jobs"
        );
    }
}

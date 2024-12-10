package at.alirezamoh.idea_whisperer_for_laravel.actions.models;

import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import com.intellij.openapi.project.Project;

/**
 * Model representing a exception
 */
public class ExceptionModel extends BaseModel {
    /**
     * @param name                      The name of the exception class
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     * @param moduleSrcPath             The module src path
     */
    public ExceptionModel(
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath
    )
    {
        super(
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.EXCEPTION_PATH,
            "Exception",
            ".php",
            "Exceptions"
        );
    }
}

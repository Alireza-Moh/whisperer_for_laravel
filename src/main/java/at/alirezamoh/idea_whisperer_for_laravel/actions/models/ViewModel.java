package at.alirezamoh.idea_whisperer_for_laravel.actions.models;

import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;

/**
 * Model representing a blade view file
 */
public class ViewModel extends BaseModel {
    /**
     * @param name                      The name of the view file
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     */
    public ViewModel(
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath
    )
    {
        super(
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.VIEW_PATH,
            "",
            ".blade.php",
            ""
        );
    }
}

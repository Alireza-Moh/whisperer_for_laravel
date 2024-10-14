package at.alirezamoh.idea_whisperer_for_laravel.actions.models;

import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;

/**
 * Model representing a Blade component view
 */
public class BladeComponentViewModel extends BaseModel {
    /**
     * @param name                      The name of the view file
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     * @param moduleSrcPath             The module src path
     */
    public BladeComponentViewModel(
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath,
        String moduleSrcPath
    )
    {
        super(
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.BLADE_COMPONENT_VIEW_PATH,
            "",
            ".blade.php",
            "",
            moduleSrcPath
        );

        if (unformattedModuleFullPath.isEmpty()) {
            this.setDestination(ProjectDefaultPaths.BLADE_COMPONENT_VIEW_PATH);
        }
        else {
            this.setDestination(this.unformattedModuleFullPath + "/" + ProjectDefaultPaths.BLADE_COMPONENT_VIEW_PATH);
        }
    }
}

package at.alirezamoh.idea_whisperer_for_laravel.actions.models;

import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import com.intellij.openapi.project.Project;

/**
 * Model representing a Blade component class
 */
public class BladeComponentClassModel extends BaseModel {
    /**
     * @param name                      The name of the component class
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     * @param moduleSrcPath             The module src path
     */
    public BladeComponentClassModel(
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
            ProjectDefaultPaths.BLADE_COMPONENT_CLASS_PATH,
            "",
            ".php",
            "View\\Components",
            moduleSrcPath
        );
    }
}

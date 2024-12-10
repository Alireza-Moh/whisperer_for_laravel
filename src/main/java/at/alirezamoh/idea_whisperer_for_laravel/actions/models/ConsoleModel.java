package at.alirezamoh.idea_whisperer_for_laravel.actions.models;

import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;

/**
 * Model representing a laravel console command
 */
public class ConsoleModel extends BaseModel {
    /**
     * the signature of the command
     */
    private String signature;

    /**
     * @param name                      The name of the console command class
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     */
    public ConsoleModel(
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath,
        String signature
    )
    {
        super(
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.CONSOLE_PATH,
            "Command",
            ".php",
            "Console\\Commands"
        );

        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }
}

package at.alirezamoh.idea_whisperer_for_laravel.actions.models;

import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import com.intellij.openapi.project.Project;

/**
 * Model representing a laravel console command
 */
public class MailableModel extends BaseModel {
    private boolean shouldQueue;

    private boolean markdownView;

    private String viewName;

    private boolean useNewSyntax;

    /**
     * @param name                      The name of the mailable class
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     * @param moduleSrcPath             The module src path
     */
    public MailableModel(
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath,
        String viewName,
        boolean shouldQueue,
        boolean markdownView,
        boolean useNewSyntax,
        String moduleSrcPath
    )
    {
        super(
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.MAILABLE_PATH,
            "Mail",
            ".php",
            "Mail",
            moduleSrcPath
        );

        this.shouldQueue = shouldQueue;
        this.markdownView = markdownView;
        this.viewName = viewName;
        this.useNewSyntax = useNewSyntax;
    }

    public boolean isShouldQueue() {
        return shouldQueue;
    }

    public boolean isMarkdownView() {
        return markdownView;
    }

    public String getViewName() {
        return viewName;
    }

    public boolean isUseNewSyntax() {
        return useNewSyntax;
    }
}

package at.alirezamoh.idea_whisperer_for_laravel.actions.models;

import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

/**
 * Model representing a laravel console command
 */
public class EventListenerModel extends BaseModel {

    private String eventClassName;

    private String eventClassPath;

    private boolean hasEventName;

    /**
     * @param name                      The name of event listener class
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     * @param moduleSrcPath             The module src path
     */
    public EventListenerModel(
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath,
        @Nullable String eventClassPath,
        String moduleSrcPath
    )
    {
        super(
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.EVENT_LISTENER_PATH,
            "Listener",
            ".php",
            "Listeners",
            moduleSrcPath
        );

        if (eventClassPath != null && !eventClassPath.isEmpty()) {
            this.eventClassName = eventClassPath.substring(eventClassPath.lastIndexOf("\\") + 1);
            this.eventClassPath = eventClassPath.substring(1);
            this.hasEventName = ! this.eventClassName.isEmpty();
        }
    }

    public String getEventClassName() {
        return eventClassName;
    }

    public String getEventClassPath() {
        return eventClassPath;
    }

    public boolean isHasEventName() {
        return hasEventName;
    }
}

package at.alirezamoh.idea_whisperer_for_laravel.support.providers;

import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a list of Laravel events in a project.
 * This class retrieves all PHP files from the "Events" directory and,
 * if the project is module-based, also from the "Events" directory within each module.
 * It then extracts the fully qualified namespace of each event and returns a list of these namespaces.
 */
public class EventProvider {
    /**
     * List to store the fully qualified namespaces of the events.
     */
    private List<String> events = new ArrayList<>();

    /**
     * The current project.
     */
    private Project project;

    /**
     * The plugin settings.
     */
    private SettingsState projectSettingState;

    /**
     * @param project             The current project
     * @param projectSettingState The plugin settings
     */
    public EventProvider(Project project, SettingsState projectSettingState) {
        this.project = project;
        this.events = new ArrayList<>();
        this.projectSettingState = projectSettingState;
    }

    /**
     * Returns a list of fully qualified namespaces of Laravel events
     * @return The list of event namespaces.
     */
    public List<String> getEvents() {
        PsiDirectory eventsDir = DirectoryPsiUtil.getDirectory(this.project, "/app/Events/");

        if (eventsDir != null) {
            for (PsiFile file : eventsDir.getFiles()) {
                if (file instanceof PhpFile eventFile) {
                    this.addModelToList(eventFile);
                }
            }
        }

        if (this.projectSettingState.isModuleApplication()) {
            this.searchForEventsInModules();
        }

        return this.events;
    }

    /**
     * Searches for events within modules in a module-based project
     */
    private void searchForEventsInModules() {
        String moduleRootPath = projectSettingState.replaceAndSlashes(projectSettingState.getModuleRootDirectoryPath());
        PsiDirectory rootDir = DirectoryPsiUtil.getDirectory(project, moduleRootPath);
        if (rootDir != null) {
            searchInModuleForEvents(rootDir);
        }
    }

    /**
     * Searches for events within each module's "Events" directory
     * @param rootDir The root directory of the modules
     */
    private void searchInModuleForEvents(PsiDirectory rootDir) {
        for (PsiDirectory module : rootDir.getSubdirectories()) {
            PsiDirectory moduleEventsDir = module.findSubdirectory("/Events/");

            if (moduleEventsDir != null) {
                for (PsiFile file : moduleEventsDir.getFiles()) {
                    if (file instanceof PhpFile eventFile) {
                        addModelToList(eventFile);
                    }
                }
            }
        }
    }

    /**
     * Adds an event's fully qualified namespace to the list
     * @param phpFile The PHP file representing the event
     */
    private void addModelToList(PhpFile phpFile) {
        VirtualFile virtualFile = phpFile.getVirtualFile();

        String namespace = phpFile.getMainNamespaceName() + "\\" + virtualFile.getNameWithoutExtension();

        this.events.add(namespace);
    }
}

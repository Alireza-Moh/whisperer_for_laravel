package at.alirezamoh.whisperer_for_laravel.support.providers;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.whisperer_for_laravel.support.utils.DirectoryUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;

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
    private List<String> events;

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
        PsiDirectory eventsDir = getDirectoryWithPath("/app/Events/");
        if (eventsDir != null) {
            processEventFiles(eventsDir);
        }

        searchInFrameworkForEvents();

        if (this.projectSettingState.isModuleApplication()) {
            searchForEventsInModules();
        }

        return events;
    }

    /**
     * Searches for events within modules in a module-based project
     */
    private void searchForEventsInModules() {
        PsiDirectory rootDir = getDirectoryWithPath(projectSettingState.getModulesDirectoryPath());

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
            String path = "/Events/";
            if (!projectSettingState.isModuleSrcDirectoryEmpty()) {
                path = StrUtils.addSlashes(
                    projectSettingState.getModuleSrcDirectoryPath(),
                    false,
                    true
                ) + path;
            }

            PsiDirectory moduleEventsDir = module.findSubdirectory(path);

            if (moduleEventsDir != null) {
                processEventFiles(moduleEventsDir);
            }
        }
    }

    /**
     * Search for events in laravel framework
     */
    private void searchInFrameworkForEvents() {
        PsiDirectory dir = getDirectoryWithPath(ProjectDefaultPaths.LARAVEL_ILLUMINATE_PATH);

        if (dir != null) {
            processEventDirectories(dir);
        }
    }

    /**
     * Recursively processes directories looking for "Events" subdirectories and adds models to the list
     *
     * @param parentDir The parent directory to start searching from
     */
    private void processEventDirectories(PsiDirectory parentDir) {
        PsiDirectory eventsDir = parentDir.findSubdirectory("Events");
        if (eventsDir != null) {
            processEventFiles(eventsDir);
        }

        for (PsiDirectory subDir : parentDir.getSubdirectories()) {
            processEventDirectories(subDir);
        }
    }

    /**
     * Retrieves a directory from the project based on the specified relative path
     * @param relativePath The relative path of the directory
     * @return The PsiDirectory or null if the directory cannot be found
     */
    private PsiDirectory getDirectoryWithPath(String relativePath) {
        String fullPath = buildFullPath(relativePath);
        return DirectoryUtils.getDirectory(this.project, fullPath);
    }

    /**
     * Builds the full directory path by appending the base path from settings
     * @param relativePath The relative path to append
     * @return The full directory path
     */
    private String buildFullPath(String relativePath) {
        String basePath = projectSettingState.isLaravelDirectoryEmpty()
            ? ""
            : StrUtils.addSlashes(
                projectSettingState.getLaravelDirectoryPath(),
                false,
                true
            );

        return basePath + relativePath;
    }

    /**
     * Processes files in the "Events" directory.
     * @param eventsDir The directory containing event files.
     */
    private void processEventFiles(PsiDirectory eventsDir) {
        for (PsiFile file : eventsDir.getFiles()) {
            if (file instanceof PhpFile) {
                addModelToList(file);
            }
        }
    }

    /**
     * Adds an event's fully qualified namespace to the list
     * @param file The PHP file representing the event
     */
    private void addModelToList(PsiFile file) {
        if (file instanceof PhpFile phpFile) {
            for (PhpClass phpClass : PhpClassUtils.getPhpClassesFromFile(phpFile)) {
                events.add(phpClass.getPresentableFQN());
            }
        }
    }
}

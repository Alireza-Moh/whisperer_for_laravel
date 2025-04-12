package at.alirezamoh.whisperer_for_laravel.packages.inertia;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.utils.DirectoryUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InertiaUtil {
    private static final String INERTIA_PACKAGE_NAME = "inertiajs/inertia-laravel";

    private static final String INERTIA_PACKAGE_DIRECTORY_PATH_IN_VENDOR = "/vendor/inertiajs/inertia-laravel";

    /**
     * Check if the project is using the inertia package
     * @param project The current project
     */
    public static boolean shouldNotCompleteOrNavigate(Project project) {
        return !PluginUtils.isLaravelProject(project)
            || !PluginUtils.isLaravelFrameworkNotInstalled(project)
            || !PluginUtils.doesPackageExistsInComposerFile(project, INERTIA_PACKAGE_NAME)
            || DirectoryUtils.getDirectory(project, INERTIA_PACKAGE_DIRECTORY_PATH_IN_VENDOR) == null;
    }

    /**
     * Retrieves the available Inertia paths from the project settings
     *
     * @param project The current project
     * @return A list of inertia paths or an empty list if none are set
     */
    public static List<String> getInertiaPaths(@NotNull Project project) {
        SettingsState settingsState = SettingsState.getInstance(project);
        String inertiaPaths = settingsState.getInertiaPageRootPath();
        if (inertiaPaths == null) {
            return new ArrayList<>();
        }

        String[] paths = inertiaPaths.split(";");
        return Arrays.stream(paths)
            .filter(path -> !path.isEmpty())
            .toList();
    }
}

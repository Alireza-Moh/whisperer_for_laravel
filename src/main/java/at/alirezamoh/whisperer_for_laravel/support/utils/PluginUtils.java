package at.alirezamoh.whisperer_for_laravel.support.utils;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

public class PluginUtils {
    private final static Logger LOG = Logger.getInstance("Whisper-For-Laravel-Plugin");

    /**
     * Retrieves the plugin's "vendor" (whisper_for_laravel) directory for the given project
     *
     * @param project The current project
     * @return A {@link PsiDirectory} pointing to the vendor directory, or {@code null} if not found
     */
    public static @Nullable PsiDirectory getPluginVendor(Project project) {
        return DirectoryUtils.getDirectory(project, ProjectDefaultPaths.WHISPERER_FOR_LARAVEL_DIR_PATH);
    }

    /**
     * Returns the Logger
     *
     * @return A {@link Logger} instance for logging plugin-related information
     */
    public static Logger getLOG() {
        return LOG;
    }

    /**
     * Checks if the Laravel framework appears uninstalled by verifying the absence
     * of the <code>/vendor/laravel/framework/</code> directory in the project
     *
     * @param project The current project
     * @return true for false
     */
    public static boolean isLaravelFrameworkNotInstalled(Project project) {
        PsiDirectory psiDirectory = DirectoryUtils.getDirectory(project, ProjectDefaultPaths.LARAVEL_VENDOR_FRAMEWORK_PATH);

        return psiDirectory == null;
    }

    /**
     * Determines whether the given project is a Laravel project by inspecting
     * the "composer.json" file for the "laravel/framework" requirement
     *
     * @param project The current project
     * @return true or false
     */
    public static boolean isLaravelProject(Project project) {
        PsiFile composerFile = getComposerFile(project);
        if (composerFile == null) {
            return false;
        }

        try {
            String fileContent = composerFile.getText();

            JsonObject jsonObject = JsonParser.parseString(fileContent).getAsJsonObject();
            JsonObject require = jsonObject.getAsJsonObject("require");

            return require != null && require.has("laravel/framework");
        } catch (Exception e) {
            LOG.error("Could not extract laravel/framework from composer file", e);
            return false;
        }
    }

    /**
     * Retrieves the Laravel version from the project's composer.json file, if present
     *
     * @param project The current project
     * @return The Laravel framework version (e.g., "9.2"), or {@code null} if not found or unreadable
     */
    public static @Nullable String laravelVersion(Project project) {
        PsiFile composerFile = getComposerFile(project);

        if (composerFile == null) {
            return null;
        }

        try {
            String fileContent = composerFile.getText();

            JsonObject jsonObject = JsonParser.parseString(fileContent).getAsJsonObject();
            JsonObject require = jsonObject.getAsJsonObject("require");

            if (require != null && require.has("laravel/framework")) {
                return require.get("laravel/framework").getAsString().replace("^", "");
            }
        } catch (Exception e) {
            LOG.error("Could not extract laravel version", e);
            return null;
        }

        return null;
    }

    /**
     * Checks if the given project is currently in "dumb" mode (indexing)
     *
     * @param project The current project
     * @return true or false
     */
    public static boolean isDumbMode(Project project) {
        return com.intellij.openapi.project.DumbService.isDumb(project);
    }

    /**
     * Retrieves the base path of the project directory, optionally appending a custom directory path
     * specified in the project settings
     *
     * @param project The current project
     * @param pathToAppendOrPrepend custom path, format = '/'
     * @param append should append to prepend
     * @return The project directory base path as a string, including the custom directory path
     *         from settings if configured, or {@code null} if settings are unavailable
     */
    public static @Nullable String getProjectDirectoryBasePath(Project project, @Nullable String pathToAppendOrPrepend, boolean append) {
        SettingsState settingsState = SettingsState.getInstance(project);

        if (settingsState == null) {
            return null;
        }

        String defaultPath = StrUtils.addSlashes(settingsState.getProjectDirectoryPath());

        if (pathToAppendOrPrepend == null) {
            return defaultPath;
        }

        if (append) {
            defaultPath = defaultPath + pathToAppendOrPrepend;
        }
        else {
            defaultPath = pathToAppendOrPrepend + defaultPath;
        }

        return StrUtils.removeDoubleSlashes(defaultPath);
    }

    /**
     * Determines whether the given package exists in the project by inspecting
     * the "composer.json" file
     *
     * @param project The current project
     * @return true or false
     */
    public static boolean doesPackageExists(Project project, String packageName) {
        PsiFile composerFile = getComposerFile(project);
        if (composerFile == null) {
            return false;
        }

        try {
            String fileContent = composerFile.getText();

            JsonObject jsonObject = JsonParser.parseString(fileContent).getAsJsonObject();
            JsonObject require = jsonObject.getAsJsonObject("require");

            return require != null && require.has(packageName);
        } catch (Exception e) {
            LOG.error("Could not extract " + packageName + " from composer file", e);
            return false;
        }
    }

    public static boolean shouldNotCompleteOrNavigate(Project project) {
        return !PluginUtils.isLaravelProject(project) || PluginUtils.isLaravelFrameworkNotInstalled(project);
    }

    /**
     * Finds the project's composer.json file based on the user-specified or default project path
     *
     * @param project The current project
     * @return A {@link PsiFile} handle for "composer.json", or {@code null} if not found
     */
    private static @Nullable PsiFile getComposerFile(Project project) {

        return DirectoryUtils.getFileByName(project, "/composer.json");
    }
}

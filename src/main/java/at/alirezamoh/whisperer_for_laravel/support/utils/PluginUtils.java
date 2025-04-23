package at.alirezamoh.whisperer_for_laravel.support.utils;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.whisperer_for_laravel.support.caching.ComposerPackageCacheManager;
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
        ComposerPackageCacheManager composerPackageCacheManager = ComposerPackageCacheManager.getInstance(project);

        return composerPackageCacheManager.isPackageInstalled("laravel/framework");
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

    public static boolean shouldNotCompleteOrNavigate(Project project) {
        return !PluginUtils.isLaravelProject(project) || PluginUtils.isLaravelFrameworkNotInstalled(project);
    }

    /**
     * Finds the project's composer.json file based on the user-specified or default project path
     *
     * @param project The current project
     * @return A {@link PsiFile} handle for "composer.json", or {@code null} if not found
     */
    public static @Nullable PsiFile getComposerFile(Project project) {

        return DirectoryUtils.getFileByName(project, "/composer.json");
    }

    /**
     * Determines whether the given package exists in the project by inspecting
     * the "composer.json" file
     *
     * @param project The current project
     * @return true or false
     */
    public static boolean doesPackageExistsInComposerFile(Project project, String packageName) {
        ComposerPackageCacheManager composerPackageCacheManager = ComposerPackageCacheManager.getInstance(project);

        return composerPackageCacheManager.isPackageInstalled(packageName);
    }
}

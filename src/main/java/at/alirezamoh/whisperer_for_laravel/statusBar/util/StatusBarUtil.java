package at.alirezamoh.whisperer_for_laravel.statusBar.util;

import at.alirezamoh.whisperer_for_laravel.support.caching.ComposerPackageCacheManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

public class StatusBarUtil {
    private StatusBarUtil() {}

    /**
     * Retrieves the Laravel version from the project's composer.json file, if present
     *
     * @param project The current project
     * @return The Laravel framework version (e.g., "9.2"), or {@code null} if not found or unreadable
     */
    public static @Nullable String laravelVersion(Project project) {
        ComposerPackageCacheManager composerPackageCacheManager = ComposerPackageCacheManager.getInstance(project);

        return composerPackageCacheManager.getInstalledVersion("laravel/framework");
    }
}

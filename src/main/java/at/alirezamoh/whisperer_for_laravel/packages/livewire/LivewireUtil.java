package at.alirezamoh.whisperer_for_laravel.packages.livewire;

import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.openapi.project.Project;

public class LivewireUtil {
    private LivewireUtil() {}

    private static final String LIVIEWIRE_PACKAGE_NAME = "livewire/livewire";

    public static boolean doNotCompleteOrNavigate(Project project) {
        return !PluginUtils.isLaravelProject(project)
            && PluginUtils.isLaravelFrameworkNotInstalled(project)
            && isLivewireNotInstalled(project);
    }

    public static boolean isLivewireNotInstalled(Project project) {
        return !PluginUtils.doesPackageExists(project, LIVIEWIRE_PACKAGE_NAME);
    }
}

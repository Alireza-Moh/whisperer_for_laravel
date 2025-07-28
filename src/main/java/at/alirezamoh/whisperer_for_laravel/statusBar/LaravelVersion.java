package at.alirezamoh.whisperer_for_laravel.statusBar;

import at.alirezamoh.whisperer_for_laravel.statusBar.util.StatusBarUtil;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class LaravelVersion implements StatusBarWidgetFactory {
    private String versionText;

    @Override
    public @NotNull @NonNls String getId() {
        return "LaravelVersion";
    }

    @Override
    public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
        return "Laravel Version";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return ApplicationManager
            .getApplication()
            .runReadAction((Computable<Boolean>) () -> {
                boolean isLaravelProject = PluginUtils.isLaravelProject(project);
                if (isLaravelProject) {
                    versionText = StatusBarUtil.laravelVersion(project);
                    return versionText != null;
                }
                return false;
            });
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new LaravelVersionStatusBarWidget(versionText);
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }
}

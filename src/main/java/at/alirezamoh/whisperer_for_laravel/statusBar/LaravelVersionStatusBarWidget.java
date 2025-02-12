package at.alirezamoh.whisperer_for_laravel.statusBar;

import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LaravelVersionStatusBarWidget implements StatusBarWidget  {
    private String versionText = "";

    public LaravelVersionStatusBarWidget(Project project) {
        ReadAction.nonBlocking(() -> PluginUtils.laravelVersion(project)).finishOnUiThread(ModalityState.nonModal(), version -> {
            if (version != null) {
                versionText = "Laravel: " + version;
            }
        }).submit(AppExecutorUtil.getAppExecutorService());
    }

    @Override
    public @NotNull String ID() {
        return "LaravelVersionStatusBarWidget";
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return new TextPresentation() {
            @Override
            public @NotNull String getText() {
                return versionText;
            }

            @Override
            public float getAlignment() {
                return 0;
            }

            @Override
            public @Nullable String getTooltipText() {
                return "Displays the Laravel framework version for the project";
            }
        };
    }
}

package at.alirezamoh.whisperer_for_laravel.extension;

import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LaravelVersionStatusBarWidget implements StatusBarWidget  {
    private final Project project;

    public LaravelVersionStatusBarWidget(Project project) {
        this.project = project;
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
                return "Laravel: " + PluginUtils.laravelVersion(project);
            }

            @Override
            public float getAlignment() {
                return 0;
            }

            @Override
            public @Nullable String getTooltipText() {
                return null;
            }
        };
    }
}

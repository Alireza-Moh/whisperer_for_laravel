package at.alirezamoh.idea_whisperer_for_laravel.settings;

import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.MethodUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LaravelStartupActivity implements ProjectActivity {

    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        if (!MethodUtils.isDumbMode(project)) {
            if (FrameworkUtils.isLaravelProject(project)) {
                SettingsState settingsState = SettingsState.getInstance(project);
                settingsState.getState();
            }
        }

        return Unit.INSTANCE;
    }
}

package at.alirezamoh.whisperer_for_laravel.settings;

import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
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
            SettingsState settingsState = SettingsState.getInstance(project);

            if (settingsState.isLaravelDirectoryEmpty()) {
                settingsState.setLaravelDirectoryPath("");
            }

            if (settingsState.getProjectType() == null) {
                settingsState.setProjectType("Standard Application");
            }

            if (settingsState.getModulesDirectoryPath() == null) {
                settingsState.setModulesDirectoryPath("Modules");
            }

            if (settingsState.isModuleSrcDirectoryEmpty()) {
                settingsState.setModuleSrcDirectoryPath("app");
            }

            if (settingsState.getInertiaPageComponentRootPath() == null) {
                settingsState.setInertiaPageComponentRootPath("resources/js/Pages;");
            }
        }

        return Unit.INSTANCE;
    }
}

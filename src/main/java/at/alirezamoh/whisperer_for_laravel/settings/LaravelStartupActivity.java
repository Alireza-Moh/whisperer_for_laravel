package at.alirezamoh.whisperer_for_laravel.settings;

import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LaravelStartupActivity implements ProjectActivity {

    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        if (!PluginUtils.isDumbMode(project)) {
            SettingsState settingsState = SettingsState.getInstance(project);

            if (settingsState.isProjectDirectoryEmpty()) {
                settingsState.setProjectDirectoryPath("");
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

            if (settingsState.getInertiaPageRootPath() == null) {
                settingsState.setInertiaPageRootPath("resources/js/Pages;");
            }

            if (!settingsState.isSuppressRealTimeFacadeWarnings()) {
                settingsState.setSuppressRealTimeFacadeWarnings(true);
            }

            if (!settingsState.isRouteNotFoundAnnotatorWarning()) {
                settingsState.setRouteNotFoundAnnotatorWarning(true);
            }
        }

        return Unit.INSTANCE;
    }
}

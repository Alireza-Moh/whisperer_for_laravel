package at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils;

import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;

public class FrameworkUtils {
    private FrameworkUtils() {}

    public static boolean isLaravelFrameworkNotInstalled(Project project) {
        PsiDirectory psiDirectory = DirectoryPsiUtil.getDirectory(project, "/vendor/laravel/framework/src/Illuminate/");

        return psiDirectory == null;
    }

    public static boolean isLaravelProject(Project project) {
        File composerFile = getComposerFile(project);
        if (!composerFile.exists()) {
            return false;
        }

        try (FileReader reader = new FileReader(composerFile)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject require = jsonObject.getAsJsonObject("require");

            return require != null && require.has("laravel/framework");
        } catch (Exception e) {
            return false;
        }
    }

    public static @Nullable String laravelVersion(Project project) {
        File composerFile = getComposerFile(project);

        try (FileReader reader = new FileReader(composerFile)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject require = jsonObject.getAsJsonObject("require");

            if (require != null && require.has("laravel/framework")) {
                return require.get("laravel/framework").getAsString().replace("^", "");
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    private static File getComposerFile(Project project) {
        SettingsState settingsState = SettingsState.getInstance(project);
        String defaultPath = project.getBasePath();

        if (!settingsState.isLaravelDirectoryEmpty()) {
            defaultPath = defaultPath + StrUtil.addSlashes(
                settingsState.getLaravelDirectoryPath(),
                false,
                true
            );
        }

        return new File(defaultPath, "composer.json");
    }
}

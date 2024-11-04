package at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils;

import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;

import java.io.File;
import java.io.FileReader;

public class FrameworkUtils {
    private FrameworkUtils() {}

    public static boolean isLaravelFrameworkNotInstalled(Project project) {
        PsiDirectory psiDirectory = DirectoryPsiUtil.getDirectory(project, "/vendor/framework/src/Illuminate/");

        return psiDirectory == null;
    }

    public static boolean isLaravelProject(Project project) {
        File composerFile = new File(project.getBasePath(), "composer.json");
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
}

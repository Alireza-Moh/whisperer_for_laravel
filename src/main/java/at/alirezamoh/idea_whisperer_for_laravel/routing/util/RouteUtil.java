package at.alirezamoh.idea_whisperer_for_laravel.routing.util;

import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;
import java.util.Collection;

public class RouteUtil {
    public static Collection<PsiFile> getAllRouteFiles(Project project) {
        Collection<PsiFile> files = new ArrayList<>();

        SettingsState settingsState = SettingsState.getInstance(project);

        if (settingsState.isModuleApplication() && settingsState.getModuleRootDirectoryPath() != null)
        {
            PsiDirectory moduleRootDir = DirectoryPsiUtil.getDirectory(project, settingsState.getModuleRootDirectoryPath());
            if (moduleRootDir != null) {
                for (PsiDirectory module : moduleRootDir.getSubdirectories()) {
                    PsiDirectory routeDir = module.findSubdirectory("routes");
                    if (routeDir != null) {
                        DirectoryPsiUtil.collectFilesRecursively(routeDir, files);
                    }
                }
            }
        }

        return files;
    }
}

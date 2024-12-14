package at.alirezamoh.idea_whisperer_for_laravel.routing.util;

import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;
import java.util.Collection;

public class RouteUtil {
    public static Collection<PsiFile> getAllRouteFiles(Project project) {
        Collection<PsiFile> files = new ArrayList<>();
        SettingsState settingsState = SettingsState.getInstance(project);

        addDefaultRouteFiles(project, settingsState, files);

        if (settingsState.isModuleApplication()) {
            addModuleRouteFiles(project, settingsState, files);
        }

        return files;
    }

    private static void addModuleRouteFiles(Project project, SettingsState settingsState, Collection<PsiFile> files) {
        String defaultModulesDirPath = StrUtil.addSlashes(settingsState.getModulesDirectoryPath());
        if (!settingsState.isLaravelDirectoryEmpty()) {
            defaultModulesDirPath = StrUtil.addSlashes(
                settingsState.getLaravelDirectoryPath(),
                false,
                true
            ) + defaultModulesDirPath;
        }

        PsiDirectory moduleRootDir = DirectoryPsiUtil.getDirectory(project, defaultModulesDirPath);

        if (moduleRootDir != null) {
            for (PsiDirectory module : moduleRootDir.getSubdirectories()) {
                PsiDirectory routeDir = module.findSubdirectory("routes");
                if (routeDir != null) {
                    DirectoryPsiUtil.collectFilesRecursively(routeDir, files);
                }
            }
        }
    }

    private static void addDefaultRouteFiles(Project project, SettingsState settingsState, Collection<PsiFile> files) {
        String defaultRouteDirPath = ProjectDefaultPaths.ROUTE_PATH;
        if (!settingsState.isLaravelDirectoryEmpty()) {
            defaultRouteDirPath = StrUtil.addSlashes(
                settingsState.getLaravelDirectoryPath(),
                false,
                true
            ) + ProjectDefaultPaths.ROUTE_PATH;
        }

        PsiDirectory rootRouteDir = DirectoryPsiUtil.getDirectory(project, defaultRouteDirPath);

        if (rootRouteDir != null) {
            DirectoryPsiUtil.collectFilesRecursively(rootRouteDir, files);
        }
    }
}

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

        if (settingsState.isModuleApplication()) {
            addRootRouteFiles(project, settingsState, files);
            addModuleRouteFiles(project, settingsState, files);
        } else {
            addDefaultRouteFiles(project, files);
        }

        return files;
    }

    private static void addRootRouteFiles(Project project, SettingsState settingsState, Collection<PsiFile> files) {
        String rootPath = settingsState.getFormattedModuleRootDirectoryPath();
        PsiDirectory rootRouteDir = null;

        if (rootPath != null) {
            rootRouteDir = DirectoryPsiUtil.getDirectory(project, rootPath + ProjectDefaultPaths.ROUTE_PATH);
        }

        if (rootRouteDir != null) {
            DirectoryPsiUtil.collectFilesRecursively(rootRouteDir, files);
        }

        addDefaultRouteFiles(project, files);
    }

    private static void addModuleRouteFiles(Project project, SettingsState settingsState, Collection<PsiFile> files) {
        PsiDirectory moduleRootDir = DirectoryPsiUtil.getDirectory(project, StrUtil.addSlashes(settingsState.getModuleRootDirectoryPath()));

        if (moduleRootDir != null) {
            for (PsiDirectory module : moduleRootDir.getSubdirectories()) {
                PsiDirectory routeDir = module.findSubdirectory("routes");
                if (routeDir != null) {
                    DirectoryPsiUtil.collectFilesRecursively(routeDir, files);
                }
            }
        }
    }

    private static void addDefaultRouteFiles(Project project, Collection<PsiFile> files) {
        PsiDirectory rootRouteDir = DirectoryPsiUtil.getDirectory(project, ProjectDefaultPaths.ROUTE_PATH);
        if (rootRouteDir != null) {
            DirectoryPsiUtil.collectFilesRecursively(rootRouteDir, files);
        }
    }
}

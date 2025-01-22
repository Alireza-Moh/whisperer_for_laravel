package at.alirezamoh.whisperer_for_laravel.support.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;

public class DirectoryUtils {
    /**
     * Retrieves a PsiDirectory object for a given directory path relative to the project base directory
     * @param project  The project containing the directory
     * @param dirName  The relative path to the directory
     * @return         The PsiDirectory object, or null if not found
     */
    public static @Nullable PsiDirectory getDirectory(Project project, String dirName) {
        dirName = PluginUtils.getProjectDirectoryBasePath(project, dirName, true);

        if (dirName == null) {
            return null;
        }

        PsiManager psiManager = PsiManager.getInstance(project);
        VirtualFile virtualFile = ProjectUtil.guessProjectDir(project);

        if (virtualFile != null) {
            virtualFile = virtualFile.findFileByRelativePath(dirName);
        }

        if (virtualFile != null) {
            return psiManager.findDirectory(virtualFile);
        }
        else {
            return null;
        }
    }

    /**
     * Retrieves a PsiFile for a given file path relative to the project base directory
     * @param project The project containing the file
     * @param path    The relative path to the file
     * @return        The PsiFile or null if not found
     */
    public static @Nullable PsiFile getFileByName(Project project, String path) {
        path = PluginUtils.getProjectDirectoryBasePath(project, path, true);
        if (path == null) {
            return null;
        }

        String fullPath = project.getBasePath() + path;

        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(fullPath);
        if (virtualFile != null) {
            return PsiManager.getInstance(project).findFile(virtualFile);
        }
        return null;
    }
}

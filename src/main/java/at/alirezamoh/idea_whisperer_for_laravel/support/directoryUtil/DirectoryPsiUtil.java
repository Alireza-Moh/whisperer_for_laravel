package at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.lang.psi.PhpFile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides utility methods for working with PsiDirectory and PsiFile objects.
 * This class offers functionalities for retrieving files recursively from a directory,
 * collecting files from subdirectories, getting a PsiDirectory by its relative path,
 * and finding a PsiFile by its name
 */
public class DirectoryPsiUtil {
    /**
     * Retrieves all files recursively from a given directory path relative to the project base directory
     * @param project         The project containing the directory
     * @param relativeDirPath The relative path to the directory
     * @return                A collection of PsiFile objects representing all files found in the directory and its subdirectories
     */
    public static Collection<PsiFile> getFilesRecursively(Project project, String relativeDirPath) {
        Collection<PsiFile> files = new ArrayList<>();

        PsiDirectory directory = getDirectory(project, relativeDirPath);
        if (directory != null) {
            collectFilesRecursively(directory, files);
        }

        return files;
    }

    /**
     * Recursively collects all files from a given directory and its subdirectories
     * @param directory The directory to start collecting files from
     * @param files     The list to store the collected files
     */
    public static void collectFilesRecursively(PsiDirectory directory, Collection<PsiFile> files) {
        for (PsiFile file : directory.getFiles()) {
            if (file instanceof PhpFile) {
                files.add(file);
            }
        }

        for (PsiDirectory subdirectory : directory.getSubdirectories()) {
            collectFilesRecursively(subdirectory, files);
        }
    }

    /**
     * Retrieves a PsiDirectory object for a given directory path relative to the project base directory
     * @param project  The project containing the directory
     * @param dirname  The relative path to the directory
     * @return         The PsiDirectory object, or null if not found
     */
    public static @Nullable PsiDirectory getDirectory(Project project, String dirname) {
        PsiManager psiManager = PsiManager.getInstance(project);

        VirtualFile virtualFile = ProjectUtil.guessProjectDir(project);
        if (virtualFile != null) {
            virtualFile = virtualFile.findFileByRelativePath(dirname);
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
        String fullPath = project.getBasePath() + path;

        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(fullPath);
        if (virtualFile != null) {
            return PsiManager.getInstance(project).findFile(virtualFile);
        }
        return null;
    }
}

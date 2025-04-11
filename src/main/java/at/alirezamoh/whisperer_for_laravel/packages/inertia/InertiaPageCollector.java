package at.alirezamoh.whisperer_for_laravel.packages.inertia;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.utils.DirectoryUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InertiaPageCollector {
    /**
     * Collect all Inertia pages based on the paths configured in SettingsState
     *
     * @param withFile Whether to include the PsiFile reference in the InertiaPage objects.
     * @return A list of discovered Inertia pages
     */
    public static List<InertiaPage> collectPages(Project project, boolean withFile) {
        List<InertiaPage> references = new ArrayList<>();
        SettingsState settings = SettingsState.getInstance(project);

        String defaultPath = "";
        if (!settings.isProjectDirectoryEmpty()) {
            defaultPath = StrUtils.removeDoubleForwardSlashes(
                StrUtils.addSlashes(settings.getProjectDirectoryPath()) + defaultPath
            );
        }

        String inertiaPaths = settings.getInertiaPageRootPath();
        if (inertiaPaths == null) {
            return references;
        }

        String[] paths = inertiaPaths.split(";");
        paths = Arrays.stream(paths)
            .filter(path -> !path.isEmpty())
            .toArray(String[]::new);

        for (String path : paths) {
            PsiDirectory potentialDir = DirectoryUtils.getDirectory(
                project,
                StrUtils.removeDoubleForwardSlashes(
                    defaultPath +
                        StrUtils.addSlashes(path.replace("\\", "/"))
                )
            );

            if (potentialDir != null) {
                references.addAll(getPagesInternal(potentialDir, withFile, ""));
            }
        }

        return references;
    }

    /**
     * Recursively collect pages from the given directory
     *
     * @param dir        The directory to scan
     * @param withFile   Whether to include a PsiFile reference in each InertiaPage
     * @param parentPath The accumulated path from root directories
     * @return           A list of Inertia pages found in this directory and its subdirectories
     */
    private static List<InertiaPage> getPagesInternal(PsiDirectory dir, boolean withFile, String parentPath) {

        List<InertiaPage> pages = new ArrayList<>(buildPagePath(dir.getFiles(), withFile, parentPath));

        for (PsiDirectory subDir : dir.getSubdirectories()) {
            String newParentPath = parentPath.isEmpty() ? subDir.getName() : parentPath + "/" + subDir.getName();
            pages.addAll(getPagesInternal(subDir, withFile, newParentPath));
        }

        return pages;
    }

    /**
     * Builds the path for all files in a single directory to detect Vue or JSX files representing Inertia pages.
     *
     * @param files      The array of files in this directory
     * @param withFile   Whether to include a PsiFile reference in each InertiaPage
     * @param parentPath The accumulated parent path segments
     * @return           A list of Inertia pages
     */
    private static List<InertiaPage> buildPagePath(PsiFile[] files, boolean withFile, String parentPath) {
        List<InertiaPage> pages = new ArrayList<>();

        for (PsiFile psiFile : files) {
            String fileName = psiFile.getName();

            if (fileName.endsWith(".vue") || fileName.endsWith(".jsx") || fileName.endsWith(".tsx")) {
                String pageName = parentPath.isEmpty() ? "" : parentPath + "/";
                pageName += fileName.replaceFirst("\\.(vue|jsx|tsx)$", "");

                if (withFile) {
                    pages.add(new InertiaPage(pageName, psiFile));
                } else {
                    pages.add(new InertiaPage(pageName));
                }
            }
        }

        return pages;
    }
}

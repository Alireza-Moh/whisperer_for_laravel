package at.alirezamoh.whisperer_for_laravel.packages.livewire;

import at.alirezamoh.whisperer_for_laravel.support.utils.DirectoryUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.json.psi.JsonFile;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LivewireUtil {
    private LivewireUtil() {}

    private static final String LIVEWIRE_PACKAGE_NAME = "livewire/livewire";

    private static final String INERTIA_PACKAGE_DIRECTORY_PATH_IN_VENDOR = "/vendor/livewire/livewire/src";

    public static boolean shouldNotCompleteOrNavigate(Project project) {
        return !PluginUtils.isLaravelProject(project)
            || PluginUtils.isLaravelFrameworkNotInstalled(project)
            || !PluginUtils.doesPackageExistsInComposerFile(project, LIVEWIRE_PACKAGE_NAME)
            || DirectoryUtils.getDirectory(project, INERTIA_PACKAGE_DIRECTORY_PATH_IN_VENDOR) == null;
    }

    public static @Nullable PsiLanguageInjectionHost getFromPsiLanguageInjectionHost(Project project, PsiElement element) {
        InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(project);

        return injectedLanguageManager.getInjectionHost(element);
    }

    public static PsiFile getFileFromPsiLanguageInjectionHost(Project project, PsiElement element) {
        PsiLanguageInjectionHost injectionHost = getFromPsiLanguageInjectionHost(project, element);

        return Objects.requireNonNullElse(injectionHost, element).getContainingFile();
    }

    public static boolean doesProjectUseTrixPackage(Project project) {
        var packageJsonVirtualFile = PackageJsonUtil.findChildPackageJsonFile(ProjectUtil.guessProjectDir(project));
        if (packageJsonVirtualFile == null) {
            return false;
        }

        PsiFile packageJson = PsiManager.getInstance(project).findFile(packageJsonVirtualFile);
        if (!(packageJson instanceof JsonFile jsonFile)) {
            return false;
        }

        return PackageJsonUtil.findDependencyByName(jsonFile, "trix") != null;
    }
}

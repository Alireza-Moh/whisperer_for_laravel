package at.alirezamoh.whisperer_for_laravel.packages.livewire;

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
import com.jetbrains.php.composer.ComposerConfigUtils;
import com.jetbrains.php.composer.InstalledPackageData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class LivewireUtil {
    private LivewireUtil() {}

    private static final String LIVEWIRE_PACKAGE_NAME = "livewire/livewire";

    public static boolean doNotCompleteOrNavigate(Project project) {
        return !PluginUtils.isLaravelProject(project)
            && PluginUtils.isLaravelFrameworkNotInstalled(project)
            && !doesProjectUseLivewirePackage(project);
    }

    public static @Nullable PsiLanguageInjectionHost getFromPsiLanguageInjectionHost(Project project, PsiElement element) {
        InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(project);

        return injectedLanguageManager.getInjectionHost(element);
    }

    public static PsiFile getFileFromPsiLanguageInjectionHost(Project project, PsiElement element) {
        PsiLanguageInjectionHost injectionHost = getFromPsiLanguageInjectionHost(project, element);

        return Objects.requireNonNullElse(injectionHost, element).getContainingFile();
    }

    public static boolean doesProjectUseLivewirePackage(Project project) {
        PsiFile psiFile = PluginUtils.getComposerFile(project);
        if (psiFile == null) {
            return false;
        }

        List<InstalledPackageData> packages = ComposerConfigUtils.getInstalledPackagesFromConfig(psiFile.getVirtualFile());
        for (InstalledPackageData packageData : packages) {
            if (packageData.getName().equals(LIVEWIRE_PACKAGE_NAME)) {
                return true;
            }
        }

        return false;
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

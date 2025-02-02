package at.alirezamoh.whisperer_for_laravel.packages.livewire;

import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LivewireUtil {
    private LivewireUtil() {}

    private static final String LIVIEWIRE_PACKAGE_NAME = "livewire/livewire";

    public static boolean doNotCompleteOrNavigate(Project project) {
        return !PluginUtils.isLaravelProject(project)
            && PluginUtils.isLaravelFrameworkNotInstalled(project)
            && isLivewireNotInstalled(project);
    }

    public static boolean isLivewireNotInstalled(Project project) {
        return !PluginUtils.doesPackageExists(project, LIVIEWIRE_PACKAGE_NAME);
    }

    public static @Nullable PsiLanguageInjectionHost getFromPsiLanguageInjectionHost(Project project, PsiElement element) {
        InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(project);

        return injectedLanguageManager.getInjectionHost(element);
    }

    public static PsiFile getFileFromPsiLanguageInjectionHost(Project project, PsiElement element) {
        PsiLanguageInjectionHost injectionHost = getFromPsiLanguageInjectionHost(project, element);

        return Objects.requireNonNullElse(injectionHost, element).getContainingFile();
    }
}

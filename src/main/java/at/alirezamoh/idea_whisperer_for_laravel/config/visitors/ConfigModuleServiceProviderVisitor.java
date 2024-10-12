package at.alirezamoh.idea_whisperer_for_laravel.config.visitors;

import at.alirezamoh.idea_whisperer_for_laravel.config.ConfigModule;
import at.alirezamoh.idea_whisperer_for_laravel.support.applicationModules.visitors.BaseServiceProviderVisitor;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Visits module service provider to collect config keys in a Laravel project
 */
public class ConfigModuleServiceProviderVisitor extends BaseServiceProviderVisitor {
    /**
     * The name of the method to look for in the service provider
     */
    private final String MERGE_CONFIG_METHOD = "mergeConfigFrom";

    private List<ConfigModule> configFilesInModule = new ArrayList<>();

    /**
     * @param project The current project
     */
    public ConfigModuleServiceProviderVisitor(Project project) {
        super(project);
    }

    /**
     * Visits an element in the service provider
     * This method specifically checks for MethodReference elements that call the
     * 'mergeConfigFrom' method and then iterates over the modules to collect config keys
     *
     * @param element The PSI element being visited
     */
    @Override
    public void visitElement(@NotNull PsiElement element) {
        if (element instanceof MethodReference methodReference) {
            String methodName = methodReference.getName();
            if (methodName != null && methodName.equals(MERGE_CONFIG_METHOD)) {
                initParameters(methodReference);
            }
        }
        super.visitElement(element);
    }

    public List<ConfigModule> getConfigFilesInModule() {
        return configFilesInModule;
    }

    /**
     * Searches for the method parameters
     * @param method method reference being visited
     */
    private void initParameters(MethodReference method) {
        String configKeyIdentifier = getSecondParameterFromMethod(method);
        String configFileName = getFirstParameterFromMethod(method);

        if (configKeyIdentifier == null || configFileName == null) {
            return;
        }

        if (moduleRootDirectoryPath != null) {
            for (PsiDirectory module : moduleRootDirectoryPath.getSubdirectories()) {
                PsiDirectory configDir = module.findSubdirectory("config");

                if (configDir != null) {
                    configFilesInModule.add(
                        new ConfigModule(StrUtil.getLastWord(configFileName), configKeyIdentifier, configDir)
                    );
                }
            }
        }
    }
}

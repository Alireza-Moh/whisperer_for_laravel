package at.alirezamoh.whisperer_for_laravel.config.resolvers;

import at.alirezamoh.whisperer_for_laravel.config.ConfigModule;
import at.alirezamoh.whisperer_for_laravel.config.visitors.ArrayReturnPsiRecursiveVisitor;
import at.alirezamoh.whisperer_for_laravel.config.visitors.ConfigModuleServiceProviderVisitor;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.whisperer_for_laravel.support.applicationModules.visitors.BaseServiceProviderVisitor;
import at.alirezamoh.whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.*;

/**
 * Collects config keys from laravel config files
 */
public class ConfigKeyCollector {
    /**
     * List of the collected config keys
     */
    private List<LookupElementBuilder> variants = new ArrayList<>();

    /**
     * Map of ArrayHashElement objects and their corresponding config key strings
     */
    private Map<PsiElement, String> configKeyWithCorrectPsiElement = new HashMap<>();

    /**
     * The current project
     */
    protected Project project;

    /**
     * The project settings
     */
    protected SettingsState projectSettingState;

    public ConfigKeyCollector(Project project) {
        this.project = project;
        this.projectSettingState = SettingsState.getInstance(project);
    }

    /**
     * Searches for config keys within a given directory
     */
    public ConfigKeyCollector startSearching() {
        String defaultConfigPath = ProjectDefaultPaths.CONFIG_PATH;
        if (!projectSettingState.isLaravelDirectoryEmpty()) {
            defaultConfigPath = StrUtil.addSlashes(
                projectSettingState.getLaravelDirectoryPath(),
                false,
                true
            ) + ProjectDefaultPaths.CONFIG_PATH;
        }

        PsiDirectory rootConfig = DirectoryPsiUtil.getDirectory(project, defaultConfigPath);
        if (rootConfig != null) {
           processDirectory(rootConfig, "");
        }

        searchForModulesConfigKeys();
        
        return this;
    }

    public ConfigKeyCollector getFromModules() {
        searchForModulesConfigKeys();

        return this;
    }

    /**
     * Returns the list the collected config keys
     * @return config keys
     */
    public List<LookupElementBuilder> getVariants() {
        return variants;
    }

    /**
     * Returns a map of ArrayHashElement objects and their corresponding config key strings
     * @return The map of ArrayHashElement objects and config key strings
     */
    public Map<PsiElement, String> getConfigKeyWithCorrectPsiElement() {
        return configKeyWithCorrectPsiElement;
    }

    /**
     * Recursively iterate a directory to collect config keys from PHP files
     *
     * @param directory      The directory to process
     * @param currentDirName The current directory name being processed
     */
    private void processDirectory(PsiDirectory directory, String currentDirName) {
        for (PsiFile file : directory.getFiles()) {
            if (file instanceof PhpFile) {
                iterateOverFileChildren(currentDirName, file, "", false, false);
            }
        }

        for (PsiDirectory subDir : directory.getSubdirectories()) {
            String dirFullPath = (currentDirName.isBlank())
                ? subDir.getName()
                : currentDirName + "." + subDir.getName();

            processDirectory(subDir, dirFullPath);
        }
    }

    /**
     * Iterates over the children of a PHP file to collect config keys
     * It uses a visitor to traverse the PSI tree and extract the keys
     * @param dirName     The name of the directory containing the file
     * @param configFile  The PHP file to process
     */
    private void iterateOverFileChildren(
        String dirName,
        PsiFile configFile,
        String configFileKeyIdentifier,
        boolean withKeyPisElement,
        boolean forModule
    ) {
        String filename = configFile.getName();
        filename = filename.substring(0, filename.length() - 4);

        ArrayReturnPsiRecursiveVisitor visitor = new ArrayReturnPsiRecursiveVisitor(
            dirName,
            filename,
            "",
            configFileKeyIdentifier,
            forModule
        );
        configFile.acceptChildren(visitor);

        String fileFullPath;
        if (forModule) {
            fileFullPath = configFileKeyIdentifier;
        }
        else  {
            fileFullPath = (dirName.isBlank())
                ? filename
                : dirName + "." + filename;
        }

        variants.add(visitor.buildLookupElement(fileFullPath, ""));
        variants.addAll(visitor.getSuggestions());

        if (withKeyPisElement) {
            configKeyWithCorrectPsiElement.putAll(visitor.getSuggestionsWithCorrectPsiElement());
            configKeyWithCorrectPsiElement.put(configFile, fileFullPath);
        }
    }

    /**
     * Searches for config keys within module config files
     */
    private void searchForModulesConfigKeys() {
        ConfigModuleServiceProviderVisitor configModuleServiceProviderVisitor = new ConfigModuleServiceProviderVisitor(this.project);

        for (PhpClass serviceProvider : BaseServiceProviderVisitor.getProviders(project)) {
            serviceProvider.acceptChildren(configModuleServiceProviderVisitor);

            List<ConfigModule> configModules = configModuleServiceProviderVisitor.getConfigFilesInModule();

            if (!configModules.isEmpty()) {
                for (ConfigModule configModule : configModules) {
                    if (configModule.configFile() instanceof PhpFile) {
                        iterateOverFileChildren(
                            "",
                            configModule.configFile(),
                            configModule.configKeyIdentifier(),
                            true,
                            true
                        );
                    }
                }
            }
        }
    }
}
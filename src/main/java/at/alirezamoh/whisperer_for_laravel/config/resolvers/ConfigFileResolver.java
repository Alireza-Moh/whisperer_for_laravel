package at.alirezamoh.whisperer_for_laravel.config.resolvers;

import at.alirezamoh.whisperer_for_laravel.config.visitors.ConfigKeyVisitor;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Resolves references to keys within Laravel config files
 */
public class ConfigFileResolver {
    /**
     * The resolved config key element
     */
    private PsiElement foundedKey;

    /**
     * The original file name
     */
    private String searchedConfigKey;

    /**
     * The visitor for traversing config keys
     */
    private ConfigKeyVisitor configKeyVisitor;
    
    private ConfigKeyCollector configKeyCollector;

    private SettingsState settingsState;

    private Project project;

    /**
     * @param project   The current project
     * @param myElement The PSI element representing the config key reference
     */
    public ConfigFileResolver(Project project, PsiElement myElement) {
        this.project = project;
        searchedConfigKey = StrUtil.removeQuotes(myElement.getText());
        configKeyVisitor = new ConfigKeyVisitor();
        configKeyCollector = new ConfigKeyCollector(project);
        settingsState = SettingsState.getInstance(project);
    }

    /**
     * Resolves the config key reference to the corresponding PSI element
     * @return The resolved config key PSI element, or null if not found
     */
    public PsiElement resolveConfigKey() {
        iterateFile();

        if (foundedKey == null) {
            searchInModules();
        }

        return foundedKey;
    }

    /**
     * Iterates through the config files to find the referenced key
     * It checks for both direct file name matches and nested key scenarios
     */
    private void iterateFile() {
        String defaultConfigPath = ProjectDefaultPaths.CONFIG_PATH;
        if (!settingsState.isLaravelDirectoryEmpty()) {
            defaultConfigPath = StrUtil.addSlashes(
                settingsState.getLaravelDirectoryPath(),
                false,
                true
            ) + defaultConfigPath;
        }

        Collection<PsiFile> configFiles = DirectoryPsiUtil.getFilesRecursively(project, defaultConfigPath);

        for (PsiFile configFile : configFiles) {
            if (!(configFile instanceof PhpFile phpFile)) {
                continue;
            }

            String configFilePath = phpFile.getVirtualFile().getPath().replace("\\", "/");
            String dirPath = project.getBasePath() + defaultConfigPath.replace("\\", "/");

            String relativePath = configFilePath.substring(dirPath.length());

            String fileNameWithoutExtension = relativePath.replace(".php", "")
                .replace("/", ".");

            if (handleDirectMatch(phpFile, fileNameWithoutExtension)) {
                return;
            } else {
                handleNestedKeys(phpFile, fileNameWithoutExtension);
            }
        }
    }

    /**
     * Handles the case where the referenced key directly matches a config file name
     * @param phpFile                  The PHP file representing the config file
     * @param fileNameWithoutExtension The name of the config file without the extension
     * @return                         True if a direct match is found, false otherwise
     */
    private boolean handleDirectMatch(PhpFile phpFile, String fileNameWithoutExtension) {
        if (fileNameWithoutExtension.equals(searchedConfigKey)) {
            foundedKey = phpFile;
            return true;
        }
        return false;
    }

    /**
     * Handles the case where the referenced key is nested within a config file
     * It iterates through the segments of the key, attempting to find a matching file
     * and then uses a visitor to locate the specific key within that file
     * @param phpFile                  The PHP file representing the config file
     * @param fileNameWithoutExtension The name of the config file without the extension
     */
    private void handleNestedKeys(PhpFile phpFile, String fileNameWithoutExtension) {
        String[] segments = searchedConfigKey.split("\\.");

        for (int i = segments.length - 1; i > 0; i--) {

            String potentialFileName = String.join(".", Arrays.copyOfRange(segments, 0, i));

            if (fileNameWithoutExtension.equals(potentialFileName)) {

                String joinedString = String.join(".", Arrays.copyOfRange(segments, i, segments.length));

                configKeyVisitor.setParts(joinedString.split("\\."));

                phpFile.acceptChildren(configKeyVisitor);

                foundedKey = configKeyVisitor.getResolvedKey();

                if (foundedKey != null) {
                    return;
                }
            }
        }
    }

    /**
     * tries to find the searched config key in the modules
     */
    private void searchInModules() {
        Map<PsiElement, String> configKeys = configKeyCollector.getFromModules().getConfigKeyWithCorrectPsiElement();

        for (Map.Entry<PsiElement, String> entry : configKeys.entrySet()) {
            if (entry.getValue().equals(searchedConfigKey)) {
                foundedKey = entry.getKey();
            }
        }
    }
}

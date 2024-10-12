package at.alirezamoh.idea_whisperer_for_laravel.config.resolvers;

import at.alirezamoh.idea_whisperer_for_laravel.config.visitors.ConfigKeyVisitor;
import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Resolves references to keys within Laravel config files
 */
public class ConfigFileResolver {
    /**
     * The config directory
     */
    private PsiDirectory configDir;

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

    /**
     * The collection of config files
     */
    private Collection<PsiFile> configFiles;
    
    private ConfigKeyCollector configKeyCollector;

    private SettingsState settingsState;

    /**
     * @param project   The current project
     * @param myElement The PSI element representing the config key reference
     */
    public ConfigFileResolver(Project project, PsiElement myElement) {
        configDir = DirectoryPsiUtil.getDirectory(project, ProjectDefaultPaths.CONFIG_PATH);
        configFiles = DirectoryPsiUtil.getFilesRecursively(project, ProjectDefaultPaths.CONFIG_PATH);
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

        if (foundedKey == null && settingsState.isModuleApplication()) {
            searchInModules();
        }

        return foundedKey;
    }

    /**
     * Iterates through the config files to find the referenced key
     * It checks for both direct file name matches and nested key scenarios
     */
    private void iterateFile() {
        for (PsiFile configFile : configFiles) {
            if (!(configFile instanceof PhpFile phpFile)) {
                continue;
            }

            String configFilePath = phpFile.getVirtualFile().getPath();
            String relativePath = configFilePath.substring(
                configDir.getVirtualFile().getPath().length() + 1
            );
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
        Map<ArrayHashElement, String> configKeys = configKeyCollector.startSearching().getConfigKeyWithCorrectPsiElement();

        for (Map.Entry<ArrayHashElement, String> entry : configKeys.entrySet()) {
            if (entry.getValue().equals(searchedConfigKey)) {
                foundedKey = entry.getKey();
            }
        }
    }
}

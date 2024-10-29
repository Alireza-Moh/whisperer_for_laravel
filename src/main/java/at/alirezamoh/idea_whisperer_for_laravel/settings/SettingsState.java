package at.alirezamoh.idea_whisperer_for_laravel.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manages the persistent state of project settings for the plugin
 * This class stores and retrieves project-specific settings, such as
 * whether the project is a Docker project, the PHP Docker container name,
 * the project type (Standard or Module-based), and the root directory path
 * for module-based projects
 */
@State(
    name="Settings",
    storages = {@Storage("idea_whisperer_for_laravel.xml")}
)
public class SettingsState implements PersistentStateComponent<SettingsState> {
    /**
     * The name of the PHP Docker container
     */
    private String phpDockerContainerName;

    /**
     * The type of the project (Standard or Module-based)
     */
    private String projectType = "Standard Application";

    /**
     * The root directory path for module-based projects
     */
    private String moduleRootDirectoryPath;

    /**
     * The root app path like "app/"
     */
    private String rootAppPath;

    /**
     * Module src path
     */
    private String moduleSrcDirectoryName;

    public static SettingsState getInstance(@NotNull Project project) {
        return project.getService(SettingsState.class);
    }

    @Override
    public @Nullable SettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull SettingsState settingsState) {
        XmlSerializerUtil.copyBean(settingsState, this);
    }

    @Override
    public void initializeComponent() {
        PersistentStateComponent.super.initializeComponent();
    }

    public String getPhpDockerContainerName() {
        return phpDockerContainerName;
    }

    public void setPhpDockerContainerName(String phpDockerContainerName) {
        this.phpDockerContainerName = phpDockerContainerName;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getModuleRootDirectoryPath() {
        return moduleRootDirectoryPath;
    }

    public void setModuleRootDirectoryPath(String moduleRootDirectoryPath) {
        this.moduleRootDirectoryPath = moduleRootDirectoryPath;
    }

    public String getRootAppPath() {
        return rootAppPath;
    }

    public void setRootAppPath(String rootAppPath) {
        this.rootAppPath = rootAppPath;
    }

    public String getModuleSrcDirectoryName() {
        return moduleSrcDirectoryName;
    }

    public void setModuleSrcDirectoryName(String moduleSrcDirectoryName) {
        this.moduleSrcDirectoryName = moduleSrcDirectoryName;
    }

    /**
     * Checks if the project is a module-based application
     * @return True if the project is a module-based application, false otherwise
     */
    public boolean isModuleApplication() {
        return "Module based Application".equals(projectType);
    }

    /**
     * Replaces backslashes with forward slashes and ensures the path starts and ends with a slash
     * @param text The text to process
     * @return The processed text
     */
    public String replaceAndSlashes(String text) {
        text = text.replace("\\", "/");

        if (!text.startsWith("/")) {
            text = "/" + text;
        }
        if (!text.endsWith("/")) {
            text = text + "/";
        }

        return text;
    }

    /**
     * Replaces backslashes with forward slashes and ensures the path starts and ends without a slash
     * @param text The text to process
     * @return The processed text
     */
    public String replaceAndSlashesToStart(String text) {
        text = text.replace("\\", "/");

        if (!text.startsWith("/")) {
            text = "/" + text;
        }
        if (text.endsWith("/")) {
            text = text.substring(0, text.length() - 1);
        }

        return text;
    }
}

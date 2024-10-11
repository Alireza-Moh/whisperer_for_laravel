package at.alirezamoh.idea_whisperer_for_laravel.settings;

import com.intellij.configurationStore.Property;
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
     * Flag indicating if the project is a Docker project
     */
    private boolean dockerProject;

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
}

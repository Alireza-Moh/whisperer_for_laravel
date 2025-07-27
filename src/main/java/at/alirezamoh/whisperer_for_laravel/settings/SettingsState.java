package at.alirezamoh.whisperer_for_laravel.settings;

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
 * the project type (Standard or Module-based), and the root directory path
 * for module-based projects
 */
@State(
    name="Settings",
    storages = {@Storage("whisperer_for_laravel.xml")}
)
public class SettingsState implements PersistentStateComponent<SettingsState> {
    private static Project project;

    /**
     * The root directory where the whole laravel project is ===> default value = the root of the opened project
     */
    private String projectDirectoryPath;

    /**
     * The type of the project (Standard or Module-based)
     */
    private String projectType;

    /**
     * The root directory path for module-based projects
     */
    private String modulesDirectoryPath;

    /**
     * The src directory path for module-based projects
     */
    private String moduleSrcDirectoryPath;

    /**
     * The path for the inertia pages components
     */
    private String inertiaPageRootPath;

    /**
     * Whether to suppress warnings for real-time facade methods: 'Undefined class', 'Undefined namespace'
     */
    private boolean suppressRealTimeFacadeWarnings;

    /**
     * Whether to inspect for route names: 'Route name not found'
     */
    private boolean routeNotFoundAnnotatorWarning;

    /**
     * Whether to inspect for translation key: 'Translation key not found'
     */
    private boolean translationKeyNotFoundAnnotatorWarning;

    public static SettingsState getInstance(@NotNull Project foundedProject) {
        project = foundedProject;
        return foundedProject.getService(SettingsState.class);
    }

    public Project getProject() {
        return project;
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

    public String getProjectDirectoryPath() {
        return projectDirectoryPath;
    }

    public void setProjectDirectoryPath(String projectDirectoryPath) {
        this.projectDirectoryPath = projectDirectoryPath;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getModulesDirectoryPath() {
        return modulesDirectoryPath;
    }

    public void setModulesDirectoryPath(String modulesDirectoryPath) {
        this.modulesDirectoryPath = modulesDirectoryPath;
    }

    public String getModuleSrcDirectoryPath() {
        return moduleSrcDirectoryPath;
    }

    public void setModuleSrcDirectoryPath(String moduleSrcDirectoryPath) {
        this.moduleSrcDirectoryPath = moduleSrcDirectoryPath;
    }

    public String getInertiaPageRootPath() {
        return inertiaPageRootPath;
    }

    public void setInertiaPageRootPath(String inertiaPageRootPath) {
        this.inertiaPageRootPath = inertiaPageRootPath;
    }

    public boolean isSuppressRealTimeFacadeWarnings() {
        return suppressRealTimeFacadeWarnings;
    }

    public void setSuppressRealTimeFacadeWarnings(boolean suppressRealTimeFacadeWarnings) {
        this.suppressRealTimeFacadeWarnings = suppressRealTimeFacadeWarnings;
    }

    public boolean isRouteNotFoundAnnotatorWarning() {
        return routeNotFoundAnnotatorWarning;
    }

    public void setRouteNotFoundAnnotatorWarning(boolean routeNotFoundAnnotatorWarning) {
        this.routeNotFoundAnnotatorWarning = routeNotFoundAnnotatorWarning;
    }

    public boolean isTranslationKeyNotFoundAnnotatorWarning() {
        return translationKeyNotFoundAnnotatorWarning;
    }

    public void setTranslationKeyNotFoundAnnotatorWarning(boolean translationKeyNotFoundAnnotatorWarning) {
        this.translationKeyNotFoundAnnotatorWarning = translationKeyNotFoundAnnotatorWarning;
    }

    public boolean isModuleApplication() {
        return "Module based Application".equals(projectType);
    }

    public boolean isProjectDirectoryEmpty() {
        return projectDirectoryPath == null || projectDirectoryPath.isEmpty();
    }

    public boolean isModuleSrcDirectoryEmpty() {
        return moduleSrcDirectoryPath == null || moduleSrcDirectoryPath.isEmpty();
    }
}

package at.alirezamoh.whisperer_for_laravel.settings;


import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class SettingConfigurable implements Configurable {
    /**
     * The project setting state
     */
    private SettingsState settingsState;

    /**
     * The UI component
     */
    private SettingsComponent settingsComponent;

    public SettingConfigurable(Project project) {
        settingsState = SettingsState.getInstance(project);
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Whisperer For Laravel";
    }

    @Override
    public @Nullable JComponent createComponent() {
        settingsComponent = new SettingsComponent();

        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        String currentProjectType = settingsComponent.getProjectType();
        String currentProjectRootDirectory = settingsComponent.getProjectRootDirectoryPath();
        String currentModuleRootDirectory = settingsComponent.getModulesDirectoryPath();
        String currentModuleSrcDirectory = settingsComponent.getModuleSrcDirectoryPath();
        String currentInertiaPageRootPath = settingsComponent.getInertiaPageRootPath();
        boolean currentSuppressRealTimeFacadeWarnings = settingsComponent.suppressRealTimeFacadeWarnings();
        boolean currentRouteNotFoundInspectionWarning = settingsComponent.getRouteNotFoundAnnotatorWarningCheckbox();
        boolean currentTranslationKeyNotFoundInspectionWarning = settingsComponent.getTranslationKeyNotFoundAnnotatorWarningCheckbox();

        String storedProjectType = settingsState.getProjectType();
        String storedProjectRootDirectory = settingsState.getProjectDirectoryPath();
        String storedModuleRootDirectory = settingsState.getModulesDirectoryPath();
        String storedModuleSrcDirectory = settingsState.getModuleSrcDirectoryPath();
        String storedInertiaPageRootPath = settingsState.getInertiaPageRootPath();
        boolean storedSuppressRealTimeFacadeWarnings = settingsState.isSuppressRealTimeFacadeWarnings();
        boolean storedRouteNotFoundInspectionWarning = settingsState.isRouteNotFoundAnnotatorWarning();
        boolean storedTranslationKeyNotFoundInspectionWarning = settingsState.isTranslationKeyNotFoundAnnotatorWarning();

        return !Objects.equals(currentProjectType, storedProjectType)
            || !Objects.equals(currentProjectRootDirectory, storedProjectRootDirectory)
            || !Objects.equals(currentModuleRootDirectory, storedModuleRootDirectory)
            || !Objects.equals(currentModuleSrcDirectory, storedModuleSrcDirectory)
            || !Objects.equals(currentInertiaPageRootPath, storedInertiaPageRootPath)
            || !Objects.equals(currentSuppressRealTimeFacadeWarnings, storedSuppressRealTimeFacadeWarnings)
            || !Objects.equals(currentRouteNotFoundInspectionWarning, storedRouteNotFoundInspectionWarning)
            || !Objects.equals(currentTranslationKeyNotFoundInspectionWarning, storedTranslationKeyNotFoundInspectionWarning);
    }

    @Override
    public void apply() throws ConfigurationException {
        validate();

        settingsState.setProjectDirectoryPath(settingsComponent.getProjectRootDirectoryPath());
        settingsState.setProjectType(settingsComponent.getProjectType());
        settingsState.setModulesDirectoryPath(settingsComponent.getModulesDirectoryPath());
        settingsState.setModuleSrcDirectoryPath(settingsComponent.getModuleSrcDirectoryPath());
        settingsState.setInertiaPageRootPath(settingsComponent.getInertiaPageRootPath());
        settingsState.setSuppressRealTimeFacadeWarnings(settingsComponent.suppressRealTimeFacadeWarnings());
        settingsState.setRouteNotFoundAnnotatorWarning(settingsComponent.getRouteNotFoundAnnotatorWarningCheckbox());
        settingsState.setTranslationKeyNotFoundAnnotatorWarning(settingsComponent.getTranslationKeyNotFoundAnnotatorWarningCheckbox());
    }

    @Override
    public void reset() {
        settingsComponent.setProjectRootDirectoryPath(settingsState.getProjectDirectoryPath());
        settingsComponent.setProjectTypeComboBox(settingsState.getProjectType());
        settingsComponent.setModulesDirectoryPathTextField(settingsState.getModulesDirectoryPath());
        settingsComponent.setModuleSrcDirectoryPathTextField(settingsState.getModuleSrcDirectoryPath());
        settingsComponent.setInertiaPageRootPath(settingsState.getInertiaPageRootPath());
        settingsComponent.setSuppressRealTimeFacadeWarnings(settingsState.isSuppressRealTimeFacadeWarnings());
        settingsComponent.setRouteNotFoundAnnotatorWarningCheckbox(settingsState.isRouteNotFoundAnnotatorWarning());
        settingsComponent.setTranslationKeyNotFoundAnnotatorWarningCheckbox(settingsState.isTranslationKeyNotFoundAnnotatorWarning());
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }

    private void validate() throws ConfigurationException {
        if (settingsComponent.getProjectType().equals("Module based Application")) {
            if (settingsComponent.getModulesDirectoryPath().isEmpty()) {
                throw new ConfigurationException("Modules directory path must not be empty.");
            }
        }
    }
}

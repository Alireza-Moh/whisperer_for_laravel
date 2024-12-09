package at.alirezamoh.idea_whisperer_for_laravel.settings;


import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

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
        return "Idea Whisperer For Laravel";
    }

    @Override
    public @Nullable JComponent createComponent() {
        settingsComponent = new SettingsComponent();

        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        return !settingsComponent.getProjectType().equals(settingsState.getProjectType())

            || !settingsComponent.getModuleRootDirectoryPath().equals(settingsState.getModuleRootDirectoryPath())

            || !settingsComponent.getModuleSrcDirectoryName().equals(settingsState.getModuleSrcDirectoryName());
    }

    @Override
    public void apply() throws ConfigurationException {
        validate();
        settingsState.setProjectType(settingsComponent.getProjectType());
        settingsState.setModuleRootDirectoryPath(settingsComponent.getModuleRootDirectoryPath());
        settingsState.setModuleSrcDirectoryName(settingsComponent.getModuleSrcDirectoryName());
    }

    @Override
    public void reset() {
        settingsComponent.setProjectTypeComboBox(settingsState.getProjectType());
        settingsComponent.setModuleRootDirectoryPathTextField(settingsState.getModuleRootDirectoryPath());
        settingsComponent.setModuleSrcDirectoryName(settingsState.getModuleSrcDirectoryName());
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }

    private void validate() throws ConfigurationException {
        if (settingsComponent.getProjectType().isEmpty()) {
            throw new ConfigurationException("Project type must not be empty.");
        }
        if (settingsComponent.getModuleRootDirectoryPath().isEmpty()) {
            throw new ConfigurationException("Module root directory path must not be empty.");
        }
        if (settingsComponent.getModuleSrcDirectoryName().isEmpty()) {
            throw new ConfigurationException("Module source directory name must not be empty.");
        }
    }
}

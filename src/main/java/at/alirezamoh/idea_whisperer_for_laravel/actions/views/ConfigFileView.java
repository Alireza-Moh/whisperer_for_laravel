package at.alirezamoh.idea_whisperer_for_laravel.actions.views;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.ConfigFileModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ConfigFileView extends BaseDialog {
    /**
     * Text field for entering the config file name
     */
    private JTextField configFileNameTextField;

    /**
     * @param project The current project
     */
    public ConfigFileView(Project project) {
        super(project);

        setTitle("Create Config File");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the Config model representing a config file
     * @return The config model
     */
    public ConfigFileModel getConfigFileModel() {
        return new ConfigFileModel(
            projectSettingState,
            configFileNameTextField.getText(),
            getUnformattedModuleFullPathForNoneAppDir(),
            getSelectedFormattedModuleFullPath()
        );
    }

    /**
     * Returns the focused component when the dialog opens
     * @return The preferred focused component
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return configFileNameTextField;
    }

    /**
     * Validates the dialog input
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = configFileNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", configFileNameTextField);
        }
        return null;
    }

    /**
     * Creates the center panel of the dialog
     * @return The center panel
     */
    @Override
    protected JComponent createCenterPanel() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());

        configFileNameTextField = new JTextField();

        initDefaultContentPaneSettings();

        gbc.insets = JBUI.insetsLeft(3);
        contentPane.add(new JLabel("Enter config file name(without '.php'):"), gbc);
        gbc.insets = JBUI.insetsLeft(0);
        gbc.insets = JBUI.insetsBottom(15);
        gbc.gridy++;
        contentPane.add(configFileNameTextField, gbc);
        configFileNameTextField.requestFocusInWindow();

        return contentPane;
    }
}

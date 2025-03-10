package at.alirezamoh.whisperer_for_laravel.actions.views;

import at.alirezamoh.whisperer_for_laravel.actions.models.ValidationRuleModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ValidationRuleView extends BaseDialog {
    /**
     * Text field for entering the job class name
     */
    private JTextField jobNameTextField;

    /**
     * @param project The current project
     */
    public ValidationRuleView(Project project) {
        super(project);

        setTitle("Create Validation Rule");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the View model representing a view file
     * @return The config model
     */
    public ValidationRuleModel getValidationRuleModel() {
        return new ValidationRuleModel(
            projectSettingState,
            jobNameTextField.getText(),
            getUnformattedModuleFullPath(moduleNameComboBox.getItem()),
            getSelectedFormattedModuleFullPath()
        );
    }

    /**
     * Returns the focused component when the dialog opens
     * @return The preferred focused component
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return jobNameTextField;
    }

    /**
     * Validates the dialog input
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = jobNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", jobNameTextField);
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

        jobNameTextField = new JTextField();

        initDefaultContentPaneSettings();

        gbc.insets = JBUI.insetsLeft(3);
        contentPane.add(new JLabel("Enter Validation class name:"), gbc);
        gbc.insets = JBUI.insetsLeft(0);
        gbc.insets = JBUI.insetsBottom(15);
        gbc.gridy++;
        contentPane.add(jobNameTextField, gbc);
        jobNameTextField.requestFocusInWindow();

        return contentPane;
    }
}

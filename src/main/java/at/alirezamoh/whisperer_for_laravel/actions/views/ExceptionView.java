package at.alirezamoh.whisperer_for_laravel.actions.views;

import at.alirezamoh.whisperer_for_laravel.actions.models.ExceptionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ExceptionView extends BaseDialog {
    /**
     * Text field for entering the exception class name
     */
    private JTextField exceptionNameTextField;

    /**
     * @param project The current project
     */
    public ExceptionView(Project project) {
        super(project);

        setTitle("Create Exception");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the Config model representing a config file
     * @return The config model
     */
    public ExceptionModel getExceptionModel() {
        return new ExceptionModel(
            projectSettingState,
            this.exceptionNameTextField.getText(),
            this.getUnformattedModuleFullPath(this.moduleNameComboBox.getItem()),
            this.getSelectedFormattedModuleFullPath()
        );
    }

    /**
     * Returns the focused component when the dialog opens
     * @return The preferred focused component
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.exceptionNameTextField;
    }

    /**
     * Validates the dialog input
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = this.exceptionNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", this.exceptionNameTextField);
        }
        return null;
    }

    /**
     * Creates the center panel of the dialog
     * @return The center panel
     */
    @Override
    protected JComponent createCenterPanel() {
        this.contentPane = new JPanel();
        this.contentPane.setLayout(new GridBagLayout());

        this.exceptionNameTextField = new JTextField();

        this.initDefaultContentPaneSettings();

        this.gbc.insets = JBUI.insetsLeft(3);
        this.contentPane.add(new JLabel("Enter Exception class name:"), this.gbc);
        this.gbc.insets = JBUI.insetsLeft(0);
        this.gbc.insets = JBUI.insetsBottom(15);
        this.gbc.gridy++;
        this.contentPane.add(this.exceptionNameTextField, this.gbc);
        this.exceptionNameTextField.requestFocusInWindow();

        return this.contentPane;
    }
}

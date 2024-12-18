package at.alirezamoh.whisperer_for_laravel.actions.views;

import at.alirezamoh.whisperer_for_laravel.actions.models.MiddlewareModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class MiddlewareView extends BaseDialog {
    /**
     * Text field for entering the middleware name
     */
    private JTextField middlewareNameTextField;

    /**
     * @param project The current project
     */
    public MiddlewareView(Project project) {
        super(project);

        setTitle("Create Middleware");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the View model representing a middleware
     * @return The config model
     */
    public MiddlewareModel getMiddlewareModel() {
        return new MiddlewareModel(
            projectSettingState,
            this.middlewareNameTextField.getText(),
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
        return this.middlewareNameTextField;
    }

    /**
     * Validates the dialog input
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = this.middlewareNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", this.middlewareNameTextField);
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

        this.middlewareNameTextField = new JTextField();

        this.initDefaultContentPaneSettings();

        this.gbc.insets = JBUI.insetsLeft(3);
        this.contentPane.add(new JLabel("Enter Middleware name:"), this.gbc);
        this.gbc.insets = JBUI.insetsLeft(0);
        this.gbc.insets = JBUI.insetsBottom(15);
        this.gbc.gridy++;
        this.contentPane.add(this.middlewareNameTextField, this.gbc);
        this.middlewareNameTextField.requestFocusInWindow();

        return this.contentPane;
    }
}

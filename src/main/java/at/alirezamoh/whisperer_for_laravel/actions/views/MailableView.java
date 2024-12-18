package at.alirezamoh.whisperer_for_laravel.actions.views;

import at.alirezamoh.whisperer_for_laravel.actions.models.MailableModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class MailableView extends BaseDialog {
    /**
     * Text field for entering the mailable class name
     */
    private JTextField mailableNameTextField;

    /**
     * Text field for entering view name
     */
    private JTextField viewNameTextField;

    private JCheckBox shouldQueueCheckBox;

    private JCheckBox markDownViewCheckBox;

    private JCheckBox useNewSyntaxCheckBox;

    /**
     * @param project The current project
     */
    public MailableView(Project project) {
        super(project);

        setTitle("Create Mailable");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the console command class
     * @return console class
     */
    public MailableModel getMailableModel() {
        return new MailableModel(
            projectSettingState,
            this.mailableNameTextField.getText(),
            this.getUnformattedModuleFullPath(this.moduleNameComboBox.getItem()),
            this.getSelectedFormattedModuleFullPath(),
            this.viewNameTextField.getText(),
            this.shouldQueueCheckBox.isSelected(),
            this.markDownViewCheckBox.isSelected(),
            this.useNewSyntaxCheckBox.isSelected()
        );
    }

    /**
     * Creates the center panel of the dialog
     * @return The center panel
     */
    @Override
    protected @Nullable JComponent createCenterPanel() {
        this.contentPane = new JPanel();
        this.contentPane.setLayout(new GridBagLayout());

        this.mailableNameTextField = new JTextField();
        this.viewNameTextField = new JTextField();
        this.shouldQueueCheckBox = new JCheckBox("Should use queue");
        this.markDownViewCheckBox = new JCheckBox("Use Markdown view");
        this.useNewSyntaxCheckBox = new JCheckBox("Use new syntax with Envelope and Content");

        this.initDefaultContentPaneSettings();

        this.gbc.insets = JBUI.insetsLeft(3);
        this.contentPane.add(new JLabel("Mailable name:"), this.gbc);
        this.gbc.gridy++;
        this.gbc.insets = JBUI.insetsLeft(0);
        this.gbc.insets = JBUI.insetsBottom(15);
        this.contentPane.add(mailableNameTextField, this.gbc);
        this.gbc.gridy++;

        this.gbc.insets = JBUI.insetsLeft(3);
        this.contentPane.add(new JLabel("View name:"), this.gbc);
        this.gbc.gridy++;
        this.gbc.insets = JBUI.insetsLeft(0);
        this.gbc.insets = JBUI.insetsBottom(15);
        this.contentPane.add(this.viewNameTextField, this.gbc);

        this.gbc.gridy++;
        this.contentPane.add(this.shouldQueueCheckBox, this.gbc);

        this.gbc.gridy++;
        this.contentPane.add(this.markDownViewCheckBox, this.gbc);

        this.gbc.gridy++;
        this.contentPane.add(this.useNewSyntaxCheckBox, this.gbc);

        return this.contentPane;
    }

    /**
     * Returns the focused component when the dialog opens
     * @return The preferred focused component
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.mailableNameTextField;
    }

    /**
     * Validates the dialog input
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = this.mailableNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", this.mailableNameTextField);
        }
        return null;
    }
}

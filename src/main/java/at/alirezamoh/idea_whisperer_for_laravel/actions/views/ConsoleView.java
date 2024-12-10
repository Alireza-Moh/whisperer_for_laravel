package at.alirezamoh.idea_whisperer_for_laravel.actions.views;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.ConsoleModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ConsoleView extends BaseDialog {
    /**
     * Text field for entering the console class name
     */
    private JTextField consoleNameTextField;

    /**
     * Text field for entering console signature
     */
    private JTextField signatureTextField;

    /**
     * @param project The current project
     */
    public ConsoleView(Project project) {
        super(project);

        setTitle("Create Console Command");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the console command class
     * @return console class
     */
    public ConsoleModel getConsoleModel() {
        return new ConsoleModel(
            consoleNameTextField.getText(),
            getUnformattedModuleFullPath(moduleNameComboBox.getItem()),
            getSelectedFormattedModuleFullPath(),
            signatureTextField.getText()
        );
    }

    /**
     * Creates the center panel of the dialog
     * @return The center panel
     */
    @Override
    protected @Nullable JComponent createCenterPanel() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());

        consoleNameTextField = new JTextField();
        signatureTextField = new JTextField();

        initDefaultContentPaneSettings();

        gbc.insets = JBUI.insetsLeft(3);
        contentPane.add(new JLabel("Console command name:"), gbc);
        gbc.gridy++;
        gbc.insets = JBUI.insetsLeft(0);
        gbc.insets = JBUI.insetsBottom(15);
        contentPane.add(consoleNameTextField, gbc);
        gbc.gridy++;

        gbc.insets = JBUI.insetsLeft(3);
        contentPane.add(new JLabel("Signature (optional):"), gbc);
        gbc.gridy++;
        gbc.insets = JBUI.insetsLeft(0);
        gbc.insets = JBUI.insetsBottom(15);
        contentPane.add(signatureTextField, gbc);

        return contentPane;
    }

    /**
     * Returns the focused component when the dialog opens
     * @return The preferred focused component
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return consoleNameTextField;
    }

    /**
     * Validates the dialog input
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = consoleNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", consoleNameTextField);
        }
        return null;
    }
}

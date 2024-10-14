package at.alirezamoh.idea_whisperer_for_laravel.actions.views;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.ControllerModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ControllerView extends BaseDialog {
    /**
     * Text field for entering the controller
     */
    private JTextField controllerNameTextField;

    /**
     * @param project The current project
     */
    public ControllerView(Project project) {
        super(project);

        setTitle("Create Controller");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the View model representing a controller
     * @return The config model
     */
    public ControllerModel getControllerModel() {
        return new ControllerModel(
           controllerNameTextField.getText(),
           getUnformattedModuleFullPath(this.moduleNameComboBox.getItem()),
           getSelectedFormattedModuleFullPath(),
            getModuleDirSrcName()
        );
    }

    /**
     * Returns the focused component when the dialog opens
     * @return The preferred focused component
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return controllerNameTextField;
    }

    /**
     * Validates the dialog input
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text =controllerNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("",controllerNameTextField);
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

       controllerNameTextField = new JTextField();

       initDefaultContentPaneSettings();

       gbc.insets = JBUI.insetsLeft(3);
       contentPane.add(new JLabel("Enter Controller name:"),gbc);
       gbc.insets = JBUI.insetsLeft(0);
       gbc.insets = JBUI.insetsBottom(15);
       gbc.gridy++;
       contentPane.add(this.controllerNameTextField,gbc);
       controllerNameTextField.requestFocusInWindow();

        return contentPane;
    }
}

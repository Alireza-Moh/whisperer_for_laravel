package at.alirezamoh.idea_whisperer_for_laravel.actions.views;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.ViewModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ViewFileView extends BaseDialog {
    /**
     * Text field for entering the blade view file name
     */
    private JTextField bladeViewFileNameTextField;

    /**
     * @param project The current project
     */
    public ViewFileView(Project project) {
        super(project);

        setTitle("Create View File");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the View model representing a view file
     * @return The config model
     */
    public ViewModel getViewFileModel() {
        return new ViewModel(
            bladeViewFileNameTextField.getText(),
            getUnformattedModuleFullPathForNoneAppDir(),
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
        return bladeViewFileNameTextField;
    }

    /**
     * Validates the dialog input
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = bladeViewFileNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", bladeViewFileNameTextField);
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

        bladeViewFileNameTextField = new JTextField();

        initDefaultContentPaneSettings();

        gbc.insets = JBUI.insetsLeft(3);
        contentPane.add(new JLabel("Enter View name:"), gbc);
        gbc.insets = JBUI.insetsLeft(0);
        gbc.insets = JBUI.insetsBottom(15);
        gbc.gridy++;
        contentPane.add(bladeViewFileNameTextField, gbc);
        bladeViewFileNameTextField.requestFocusInWindow();

        return contentPane;
    }
}

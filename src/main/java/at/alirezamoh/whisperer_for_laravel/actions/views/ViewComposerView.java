package at.alirezamoh.whisperer_for_laravel.actions.views;

import at.alirezamoh.whisperer_for_laravel.actions.models.ControllerModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.ViewComposerModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ViewComposerView extends BaseDialog {
    /**
     * Text field for entering the view composer
     */
    private JTextField viewComposerNameTextField;

    /**
     * @param project The current project
     */
    public ViewComposerView(Project project) {
        super(project);

        setTitle("Create View Composer");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the View model representing a viewc omposer
     *
     * @return The config model
     */
    public ViewComposerModel getViewComposerModel() {
        return new ViewComposerModel(
            projectSettingState,
            viewComposerNameTextField.getText(),
            getUnformattedModuleFullPath(this.moduleNameComboBox.getItem()),
            getSelectedFormattedModuleFullPath()
        );
    }

    /**
     * Returns the focused component when the dialog opens
     *
     * @return The preferred focused component
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return viewComposerNameTextField;
    }

    /**
     * Validates the dialog input
     *
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = viewComposerNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", viewComposerNameTextField);
        }
        return null;
    }

    /**
     * Creates the center panel of the dialog
     *
     * @return The center panel
     */
    @Override
    protected JComponent createCenterPanel() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());

        viewComposerNameTextField = new JTextField();

        initDefaultContentPaneSettings();

        gbc.insets = JBUI.insetsLeft(3);
        contentPane.add(new JLabel("Enter View Composer name:"), gbc);
        gbc.insets = JBUI.insetsLeft(0);
        gbc.insets = JBUI.insetsBottom(15);
        gbc.gridy++;
        contentPane.add(this.viewComposerNameTextField, gbc);
        viewComposerNameTextField.requestFocusInWindow();

        return contentPane;
    }
}

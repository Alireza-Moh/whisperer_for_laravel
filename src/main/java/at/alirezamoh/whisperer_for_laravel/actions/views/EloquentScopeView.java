package at.alirezamoh.whisperer_for_laravel.actions.views;

import at.alirezamoh.whisperer_for_laravel.actions.models.ControllerModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.EloquentScopeModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class EloquentScopeView extends BaseDialog {
    /**
     * Text field for entering the scope
     */
    private JTextField eloquentScopeNameTextField;

    /**
     * @param project The current project
     */
    public EloquentScopeView(Project project) {
        super(project);

        setTitle("Create Eloquent Scope");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the View model representing a eloquent scope
     *
     * @return The config model
     */
    public EloquentScopeModel getEloquentScopeModel() {
        return new EloquentScopeModel(
            projectSettingState,
            eloquentScopeNameTextField.getText(),
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
        return eloquentScopeNameTextField;
    }

    /**
     * Validates the dialog input
     *
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = eloquentScopeNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", eloquentScopeNameTextField);
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

        eloquentScopeNameTextField = new JTextField();

        initDefaultContentPaneSettings();

        gbc.insets = JBUI.insetsLeft(3);
        contentPane.add(new JLabel("Enter Eloquent Scope name:"), gbc);
        gbc.insets = JBUI.insetsLeft(0);
        gbc.insets = JBUI.insetsBottom(15);
        gbc.gridy++;
        contentPane.add(this.eloquentScopeNameTextField, gbc);
        eloquentScopeNameTextField.requestFocusInWindow();

        return contentPane;
    }
}

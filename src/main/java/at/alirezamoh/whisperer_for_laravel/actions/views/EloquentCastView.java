package at.alirezamoh.whisperer_for_laravel.actions.views;

import at.alirezamoh.whisperer_for_laravel.actions.models.EloquentCastModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.EloquentScopeModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class EloquentCastView extends BaseDialog {
    /**
     * Text field for entering the cast
     */
    private JTextField eloquentCastNameTextField;

    /**
     * @param project The current project
     */
    public EloquentCastView(Project project) {
        super(project);

        setTitle("Create Eloquent Cast");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the View model representing an eloquent cast
     *
     * @return The config model
     */
    public EloquentCastModel getEloquentCastModel() {
        return new EloquentCastModel(
            projectSettingState,
            eloquentCastNameTextField.getText(),
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
        return eloquentCastNameTextField;
    }

    /**
     * Validates the dialog input
     *
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = eloquentCastNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", eloquentCastNameTextField);
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

        eloquentCastNameTextField = new JTextField();

        initDefaultContentPaneSettings();

        gbc.insets = JBUI.insetsLeft(3);
        contentPane.add(new JLabel("Enter Eloquent Cast name:"), gbc);
        gbc.insets = JBUI.insetsLeft(0);
        gbc.insets = JBUI.insetsBottom(15);
        gbc.gridy++;
        contentPane.add(this.eloquentCastNameTextField, gbc);
        eloquentCastNameTextField.requestFocusInWindow();

        return contentPane;
    }
}

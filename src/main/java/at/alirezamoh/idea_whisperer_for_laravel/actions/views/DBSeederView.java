package at.alirezamoh.idea_whisperer_for_laravel.actions.views;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.DBSeederModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DBSeederView extends BaseDialog {
    /**
     * Text field for entering the Database Seeder name
     */
    private JTextField dbSeederNameTextField;

    /**
     * @param project The current project
     */
    public DBSeederView(Project project) {
        super(project);

        setTitle("Create Database Seeder");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the View model representing a middleware
     * @return The config model
     */
    public DBSeederModel getDBSeederModel() {
        String unformattedModuleFullPath = getUnformattedModuleFullPath(moduleNameComboBox.getItem());
        if (!projectSettingState.isModuleApplication()) {
            unformattedModuleFullPath = "";
        }

        return new DBSeederModel(
            this.dbSeederNameTextField.getText(),
            unformattedModuleFullPath,
            this.getSelectedFormattedModuleFullPath()
        );
    }

    /**
     * Returns the focused component when the dialog opens
     * @return The preferred focused component
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.dbSeederNameTextField;
    }

    /**
     * Validates the dialog input
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = this.dbSeederNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", this.dbSeederNameTextField);
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

        this.dbSeederNameTextField = new JTextField();

        this.initDefaultContentPaneSettings();

        this.gbc.insets = JBUI.insetsLeft(3);
        this.contentPane.add(new JLabel("Enter Database Seeder name:"), this.gbc);
        this.gbc.insets = JBUI.insetsLeft(0);
        this.gbc.insets = JBUI.insetsBottom(15);
        this.gbc.gridy++;
        this.contentPane.add(this.dbSeederNameTextField, this.gbc);
        this.dbSeederNameTextField.requestFocusInWindow();

        return this.contentPane;
    }
}

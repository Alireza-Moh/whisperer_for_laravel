package at.alirezamoh.idea_whisperer_for_laravel.actions.views;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.MigrationModel;
import com.intellij.ide.HelpTooltip;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class MigrationView extends BaseDialog {
    /**
     * Text field for entering the migration class name
     */
    private JTextField migrationClassNameTextField;

    /**
     * Text field for entering the table  name
     */
    private JTextField migrationTableNameTextField;

    /**
     * Is it a changing or creation action
     */
    private JRadioButton createTableRadioButton;

    /**
     * Is it a changing or creation action
     */
    private JRadioButton changeTableRadioButton;

    /**
     * Should be an anonymous class
     */
    private JBCheckBox anonymousCheckBox;

    /**
     * @param project The current project
     */
    public MigrationView(Project project) {
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
    public MigrationModel getMigrationModel() {
        String unformattedModuleFullPath = getUnformattedModuleFullPath(moduleNameComboBox.getItem());
        if (!projectSettingState.isModuleApplication()) {
            unformattedModuleFullPath = "";
        }

        return new MigrationModel(
            this.migrationClassNameTextField.getText(),
            unformattedModuleFullPath,
            this.getSelectedFormattedModuleFullPath(),
            migrationTableNameTextField.getText(),
            createTableRadioButton.isSelected(),
            false,
            anonymousCheckBox.isSelected()
        );
    }

    /**
     * Returns the focused component when the dialog opens
     * @return The preferred focused component
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.migrationClassNameTextField;
    }

    /**
     * Validates the dialog input
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = this.migrationClassNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", this.migrationClassNameTextField);
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

        this.migrationClassNameTextField = new JTextField();
        this.migrationTableNameTextField = new JTextField();
        this.createTableRadioButton = new JRadioButton("Create Table");
        this.changeTableRadioButton = new JRadioButton("Change Table", true);
        this.anonymousCheckBox = new JBCheckBox("Anonymous class");

        new HelpTooltip()
            .setLocation(HelpTooltip.Alignment.RIGHT)
            .setDescription("The migration file name will be created based on the class name. The class name will be ignored when anonymous is activated")
            .installOn(this.migrationClassNameTextField);

        this.initDefaultContentPaneSettings();

        ButtonGroup tableActionGroup = new ButtonGroup();
        tableActionGroup.add(this.createTableRadioButton);
        tableActionGroup.add(this.changeTableRadioButton);

        this.gbc.insets = JBUI.insetsLeft(3);
        this.contentPane.add(new JLabel("Migration class name:"), this.gbc);
        this.gbc.insets = JBUI.insetsLeft(0);
        this.gbc.insets = JBUI.insetsBottom(15);
        this.gbc.gridy++;
        this.contentPane.add(this.migrationClassNameTextField, this.gbc);

        this.gbc.gridy++;
        this.gbc.insets = JBUI.insetsLeft(3);
        this.contentPane.add(new JLabel("Table Name:"), this.gbc);
        this.gbc.insets = JBUI.insetsLeft(0);
        this.gbc.insets = JBUI.insetsBottom(15);
        this.gbc.gridy++;
        this.contentPane.add(this.migrationTableNameTextField, this.gbc);

        this.gbc.gridy++;
        this.contentPane.add(this.createTableRadioButton, gbc);

        this.gbc.gridy++;
        this.contentPane.add(this.changeTableRadioButton, gbc);

        this.gbc.gridy++;
        this.contentPane.add(this.anonymousCheckBox, gbc);

        return this.contentPane;
    }
}

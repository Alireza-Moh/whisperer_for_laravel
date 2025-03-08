package at.alirezamoh.whisperer_for_laravel.actions.views;

import at.alirezamoh.whisperer_for_laravel.actions.models.DbFactoryModel;
import at.alirezamoh.whisperer_for_laravel.actions.views.providers.TextFieldAutoCompletionProvider;
import at.alirezamoh.whisperer_for_laravel.support.providers.ModelProvider;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DbFactoryView extends BaseDialog {
    /**
     * Text field for entering the db factory class name
     */
    private JTextField dbFactoryNameTextField;

    /**
     * Select input for selecting eloquent model
     */
    private TextFieldWithAutoCompletion eloquentModelNameTextField;

    /**
     * Service class for collecting eloquent Models
     */
    private ModelProvider modelProvider;

    /**
     * @param project The current project
     */
    public DbFactoryView(Project project) {
        super(project);

        this.modelProvider = new ModelProvider(project);

        setTitle("Create Database Factory");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the db factory class model
     * @return policy class model
     */
    public DbFactoryModel getDbFactoryModel() {
        return new DbFactoryModel(
            projectSettingState,
            this.dbFactoryNameTextField.getText(),
            this.getUnformattedModuleFullPath(this.moduleNameComboBox.getItem()),
            this.getSelectedFormattedModuleFullPath(),
            this.eloquentModelNameTextField.getText()
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

        this.dbFactoryNameTextField = new JTextField();
        this.eloquentModelNameTextField = new TextFieldWithAutoCompletion(
            this.project,
            new TextFieldAutoCompletionProvider(this.modelProvider.getModels(), AllIcons.Nodes.Class),
            true,
            ""
        );

        this.initDefaultContentPaneSettings();

        this.gbc.insets = JBUI.insetsLeft(3);
        this.contentPane.add(new JLabel("DB Factory name:"), this.gbc);
        this.gbc.gridy++;
        this.gbc.insets = JBUI.insetsLeft(0);
        this.gbc.insets = JBUI.insetsBottom(15);
        this.contentPane.add(this.dbFactoryNameTextField, this.gbc);
        this.gbc.gridy++;

        this.gbc.insets = JBUI.insetsLeft(3);
        this.contentPane.add(new JLabel("Eloquent Model (optional):"), this.gbc);
        this.gbc.gridy++;
        this.gbc.insets = JBUI.insetsLeft(0);
        this.gbc.insets = JBUI.insetsBottom(15);
        this.contentPane.add(this.eloquentModelNameTextField, this.gbc);

        return this.contentPane;
    }

    /**
     * Returns the focused component when the dialog opens
     * @return The preferred focused component
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.dbFactoryNameTextField;
    }

    /**
     * Validates the dialog input
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = this.dbFactoryNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", this.dbFactoryNameTextField);
        }
        return null;
    }
}

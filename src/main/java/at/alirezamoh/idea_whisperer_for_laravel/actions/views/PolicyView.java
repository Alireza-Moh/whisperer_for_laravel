package at.alirezamoh.idea_whisperer_for_laravel.actions.views;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.PolicyModel;
import at.alirezamoh.idea_whisperer_for_laravel.actions.views.providers.TextFieldAutoCompletionProvider;
import at.alirezamoh.idea_whisperer_for_laravel.support.providers.ModelProvider;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class PolicyView extends BaseDialog {
    /**
     * Text field for entering the policy class name
     */
    private JTextField policyNameTextField;

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
    public PolicyView(Project project) {
        super(project);

        this.modelProvider = new ModelProvider(project, projectSettingState);

        setTitle("Create Policy");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the policy class model
     * @return policy class model
     */
    public PolicyModel getPolicyModel() {
        return new PolicyModel(
            projectSettingState,
            this.policyNameTextField.getText(),
            this.eloquentModelNameTextField.getText(),
            this.getUnformattedModuleFullPath(this.moduleNameComboBox.getItem()),
            this.getSelectedFormattedModuleFullPath()
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

        this.policyNameTextField = new JTextField();
        this.eloquentModelNameTextField = new TextFieldWithAutoCompletion(
            this.project,
            new TextFieldAutoCompletionProvider(this.modelProvider.getModels(), AllIcons.Nodes.Class),
            true,
            ""
        );

        this.initDefaultContentPaneSettings();

        this.gbc.insets = JBUI.insetsLeft(3);
        this.contentPane.add(new JLabel("Observer class name:"), this.gbc);
        this.gbc.gridy++;
        this.gbc.insets = JBUI.insetsLeft(0);
        this.gbc.insets = JBUI.insetsBottom(15);
        this.contentPane.add(this.policyNameTextField, this.gbc);
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
        return this.policyNameTextField;
    }

    /**
     * Validates the dialog input
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = this.policyNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", this.policyNameTextField);
        }
        return null;
    }
}

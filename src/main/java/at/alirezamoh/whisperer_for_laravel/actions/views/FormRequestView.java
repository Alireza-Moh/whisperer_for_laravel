package at.alirezamoh.whisperer_for_laravel.actions.views;

import at.alirezamoh.whisperer_for_laravel.actions.models.FormRequestModel;
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

public class FormRequestView extends BaseDialog {
    /**
     * Text field for entering the form request class name
     */
    private JTextField formRequestNameTextField;

    /**
     * Select input for selecting eloquent model
     */
    private TextFieldWithAutoCompletion eloquentModelNameTextField;

    private JCheckBox authorizeCheckBox;

    /**
     * Service class for collecting eloquent Models
     */
    private ModelProvider modelProvider;

    /**
     * @param project The current project
     */
    public FormRequestView(Project project) {
        super(project);

        this.modelProvider = new ModelProvider(project);

        setTitle("Create Form Request");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the JsonResource class model
     * @return policy class model
     */
    public FormRequestModel getFormRequestModel() {
        return new FormRequestModel(
            projectSettingState,
            this.formRequestNameTextField.getText(),
            this.getUnformattedModuleFullPath(this.moduleNameComboBox.getItem()),
            this.getSelectedFormattedModuleFullPath(),
            this.eloquentModelNameTextField.getText(),
            this.authorizeCheckBox.isSelected()
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

        this.formRequestNameTextField = new JTextField();
        this.eloquentModelNameTextField = new TextFieldWithAutoCompletion(
            this.project,
            new TextFieldAutoCompletionProvider(this.modelProvider.getModels(), AllIcons.Nodes.Class),
            true,
            ""
        );
        this.authorizeCheckBox = new JCheckBox("Add authorize method");

        this.initDefaultContentPaneSettings();

        this.gbc.insets = JBUI.insetsLeft(3);
        this.contentPane.add(new JLabel("Form Request name:"), this.gbc);
        this.gbc.gridy++;
        this.gbc.insets = JBUI.insetsLeft(0);
        this.gbc.insets = JBUI.insetsBottom(15);
        this.contentPane.add(this.formRequestNameTextField, this.gbc);
        this.gbc.gridy++;

        this.gbc.insets = JBUI.insetsLeft(3);
        this.contentPane.add(new JLabel("Eloquent Model (optional):"), this.gbc);
        this.gbc.gridy++;
        this.gbc.insets = JBUI.insetsLeft(0);
        this.gbc.insets = JBUI.insetsBottom(15);
        this.contentPane.add(this.eloquentModelNameTextField, this.gbc);

        this.gbc.gridy++;
        this.contentPane.add(this.authorizeCheckBox, gbc);

        return this.contentPane;
    }

    /**
     * Returns the focused component when the dialog opens
     * @return The preferred focused component
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.formRequestNameTextField;
    }

    /**
     * Validates the dialog input
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = this.formRequestNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", this.formRequestNameTextField);
        }
        return null;
    }
}

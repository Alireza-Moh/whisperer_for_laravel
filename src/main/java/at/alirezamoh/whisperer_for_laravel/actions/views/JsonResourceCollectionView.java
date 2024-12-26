package at.alirezamoh.whisperer_for_laravel.actions.views;

import at.alirezamoh.whisperer_for_laravel.actions.models.JsonResourceCollectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class JsonResourceCollectionView extends BaseDialog {
    /**
     * Text field for entering the json resource class name
     */
    private JTextField jsonResourceNameTextField;

    /**
     * @param project The current project
     */
    public JsonResourceCollectionView(Project project) {
        super(project);

        setTitle("Create Json Resource");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the JsonResource class model
     * @return policy class model
     */
    public JsonResourceCollectionModel getJsonResourceCollectionModel() {
        return new JsonResourceCollectionModel(
            projectSettingState,
            this.jsonResourceNameTextField.getText(),
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

        this.jsonResourceNameTextField = new JTextField();

        this.initDefaultContentPaneSettings();

        this.gbc.insets = JBUI.insetsLeft(3);
        this.contentPane.add(new JLabel("Json Resource Collection name:"), this.gbc);
        this.gbc.gridy++;
        this.gbc.insets = JBUI.insetsLeft(0);
        this.gbc.insets = JBUI.insetsBottom(15);
        this.contentPane.add(this.jsonResourceNameTextField, this.gbc);
        this.gbc.gridy++;

        return this.contentPane;
    }

    /**
     * Returns the focused component when the dialog opens
     * @return The preferred focused component
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.jsonResourceNameTextField;
    }

    /**
     * Validates the dialog input
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = this.jsonResourceNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", this.jsonResourceNameTextField);
        }
        return null;
    }
}

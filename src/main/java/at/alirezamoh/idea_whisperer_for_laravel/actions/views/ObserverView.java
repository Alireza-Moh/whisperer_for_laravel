package at.alirezamoh.idea_whisperer_for_laravel.actions.views;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.ObserverModel;
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

public class ObserverView extends BaseDialog {
    /**
     * Text field for entering the observer class name
     */
    private JTextField observerNameTextField;

    /**
     * Select input for selecting eloquent model
     */
    private TextFieldWithAutoCompletion eloquentModelNameTextField;

    /**
     * Service class for collecting eloquent Models
     */
    private ModelProvider modelProvider;

    private JCheckBox addCreatingMethodCheckBox;

    private JCheckBox addCreatedMethodCheckBox;

    private JCheckBox addUpdatingMethodCheckBox;

    private JCheckBox addUpdatedMethodCheckBox;

    private JCheckBox addSavingMethodCheckBox;

    private JCheckBox addSavedMethodCheckBox;

    private JCheckBox addDeletingMethodCheckBox;

    private JCheckBox addDeletedMethodCheckBox;

    private JCheckBox addRestoringMethodCheckBox;

    private JCheckBox addRestoredMethodCheckBox;

    private JCheckBox addRetrievedMethodCheckBox;

    private JCheckBox addForceDeletingMethodCheckBox;

    private JCheckBox addForceDeletedMethodCheckBox;

    private JCheckBox addReplicatingMethodCheckBox;

    /**
     * @param project The current project
     */
    public ObserverView(Project project) {
        super(project);

        this.modelProvider = new ModelProvider(project);

        setTitle("Create Observer");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the observer class model
     * @return policy class model
     */
    public ObserverModel getObserverModel() {
        return new ObserverModel(
            projectSettingState,
            this.observerNameTextField.getText(),
            this.eloquentModelNameTextField.getText(),
            this.getUnformattedModuleFullPath(this.moduleNameComboBox.getItem()),
            this.getSelectedFormattedModuleFullPath(),
            this.addCreatingMethodCheckBox.isSelected(),
            this.addCreatedMethodCheckBox.isSelected(),
            this.addUpdatingMethodCheckBox.isSelected(),
            this.addUpdatedMethodCheckBox.isSelected(),
            this.addSavingMethodCheckBox.isSelected(),
            this.addSavedMethodCheckBox.isSelected(),
            this.addDeletingMethodCheckBox.isSelected(),
            this.addDeletedMethodCheckBox.isSelected(),
            this.addRestoringMethodCheckBox.isSelected(),
            this.addRestoredMethodCheckBox.isSelected(),
            this.addRetrievedMethodCheckBox.isSelected(),
            this.addForceDeletingMethodCheckBox.isSelected(),
            this.addForceDeletedMethodCheckBox.isSelected(),
            this.addReplicatingMethodCheckBox.isSelected()
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

        this.observerNameTextField = new JTextField();
        this.eloquentModelNameTextField = new TextFieldWithAutoCompletion(
            this.project,
            new TextFieldAutoCompletionProvider(this.modelProvider.getModels(), AllIcons.Nodes.Class),
            true,
            ""
        );
        this.addCreatingMethodCheckBox = new JCheckBox("Add 'creating' method");
        this.addCreatedMethodCheckBox = new JCheckBox("Add 'created' method");
        this.addUpdatingMethodCheckBox = new JCheckBox("Add 'updating' method");
        this.addUpdatedMethodCheckBox = new JCheckBox("Add 'updated' method");
        this.addSavingMethodCheckBox = new JCheckBox("Add 'saving' method");
        this.addSavedMethodCheckBox = new JCheckBox("Add 'saved' method");
        this.addDeletingMethodCheckBox = new JCheckBox("Add 'deleting' method");
        this.addDeletedMethodCheckBox = new JCheckBox("Add 'deleted' method");
        this.addRestoringMethodCheckBox = new JCheckBox("Add 'restoring' method");
        this.addRestoredMethodCheckBox = new JCheckBox("Add 'restored' method");
        this.addRetrievedMethodCheckBox = new JCheckBox("Add 'retrieved' method");
        this.addForceDeletingMethodCheckBox = new JCheckBox("Add 'forceDeleting' method");
        this.addForceDeletedMethodCheckBox = new JCheckBox("Add 'forceDeleted' method");
        this.addReplicatingMethodCheckBox = new JCheckBox("Add 'replicating' method");

        this.initDefaultContentPaneSettings();

        this.gbc.insets = JBUI.insetsLeft(3);
        this.contentPane.add(new JLabel("Observer class name:"), this.gbc);
        this.gbc.gridy++;
        this.gbc.insets = JBUI.insetsLeft(0);
        this.gbc.insets = JBUI.insetsBottom(15);
        this.contentPane.add(this.observerNameTextField, this.gbc);
        this.gbc.gridy++;

        this.gbc.insets = JBUI.insetsLeft(3);
        this.contentPane.add(new JLabel("Eloquent Model (optional):"), this.gbc);
        this.gbc.gridy++;
        this.gbc.insets = JBUI.insetsLeft(0);
        this.gbc.insets = JBUI.insetsBottom(15);
        this.contentPane.add(this.eloquentModelNameTextField, this.gbc);

        this.gbc.gridy++;
        this.contentPane.add(this.addCreatingMethodCheckBox, gbc);

        this.gbc.gridy++;
        this.contentPane.add(this.addCreatedMethodCheckBox, gbc);

        this.gbc.gridy++;
        this.contentPane.add(this.addUpdatingMethodCheckBox, gbc);

        this.gbc.gridy++;
        this.contentPane.add(this.addUpdatedMethodCheckBox, gbc);

        this.gbc.gridy++;
        this.contentPane.add(this.addSavingMethodCheckBox, gbc);

        this.gbc.gridy++;
        this.contentPane.add(this.addSavedMethodCheckBox, gbc);

        this.gbc.gridy++;
        this.contentPane.add(this.addDeletedMethodCheckBox, gbc);

        this.gbc.gridy++;
        this.contentPane.add(this.addDeletingMethodCheckBox, gbc);

        this.gbc.gridy++;
        this.contentPane.add(this.addRestoringMethodCheckBox, gbc);

        this.gbc.gridy++;
        this.contentPane.add(this.addRestoredMethodCheckBox, gbc);

        this.gbc.gridy++;
        this.contentPane.add(this.addReplicatingMethodCheckBox, gbc);

        return this.contentPane;
    }

    /**
     * Returns the focused component when the dialog opens
     * @return The preferred focused component
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.observerNameTextField;
    }

    /**
     * Validates the dialog input
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = this.observerNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", this.observerNameTextField);
        }
        return null;
    }
}

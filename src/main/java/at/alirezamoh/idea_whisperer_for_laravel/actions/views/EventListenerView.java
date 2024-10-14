package at.alirezamoh.idea_whisperer_for_laravel.actions.views;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.EventListenerModel;
import at.alirezamoh.idea_whisperer_for_laravel.actions.views.providers.TextFieldAutoCompletionProvider;
import at.alirezamoh.idea_whisperer_for_laravel.support.providers.EventProvider;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class EventListenerView extends BaseDialog {
    /**
     * Text field for entering the console class name
     */
    private JTextField eventListenerTextField;

    private TextFieldWithAutoCompletion eventClassTextField;

    private EventProvider eventProvider;

    /**
     * @param project The current project
     */
    public EventListenerView(Project project) {
        super(project);

        this.eventProvider = new EventProvider(project, projectSettingState);

        setTitle("Create Event Listener");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the event listener class
     * @return event listener class
     */
    public EventListenerModel getEventListenerModel() {
        return new EventListenerModel(
            this.eventListenerTextField.getText(),
            this.getUnformattedModuleFullPath(moduleNameComboBox.getItem()),
            this.getSelectedFormattedModuleFullPath(),
            eventClassTextField.getText(),
            getModuleDirSrcName()
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

        this.eventListenerTextField = new JTextField();
        this.eventClassTextField = new TextFieldWithAutoCompletion(
            this.project,
            new TextFieldAutoCompletionProvider(this.eventProvider.getEvents(), AllIcons.Nodes.Class),
            true,
            ""
        );

        this.initDefaultContentPaneSettings();

        this.gbc.insets = JBUI.insetsLeft(3);
        this.contentPane.add(new JLabel("Event Listener Name:"), this.gbc);
        this.gbc.gridy++;
        this.gbc.insets = JBUI.insetsLeft(0);
        this.gbc.insets = JBUI.insetsBottom(15);
        this.contentPane.add(eventListenerTextField, this.gbc);
        this.gbc.gridy++;

        this.gbc.insets = JBUI.insetsLeft(3);
        this.contentPane.add(new JLabel("Event Class (optional):"), this.gbc);
        this.gbc.gridy++;
        this.gbc.insets = JBUI.insetsLeft(0);
        this.gbc.insets = JBUI.insetsBottom(15);
        this.contentPane.add(this.eventClassTextField, this.gbc);

        return this.contentPane;
    }

    /**
     * Returns the focused component when the dialog opens
     * @return The preferred focused component
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.eventListenerTextField;
    }

    /**
     * Validates the dialog input
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = this.eventListenerTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", this.eventListenerTextField);
        }
        return null;
    }
}

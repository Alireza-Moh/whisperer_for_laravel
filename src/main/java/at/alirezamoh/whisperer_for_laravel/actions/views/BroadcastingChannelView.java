package at.alirezamoh.whisperer_for_laravel.actions.views;

import at.alirezamoh.whisperer_for_laravel.actions.models.BroadcastingChannelModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.ControllerModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class BroadcastingChannelView extends BaseDialog {
    /**
     * Text field for entering the broadcasting channel
     */
    private JTextField broadcastingChannelNameTextField;

    /**
     * @param project The current project
     */
    public BroadcastingChannelView(Project project) {
        super(project);

        setTitle("Create Broadcasting Channel");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the View model representing a BroadcastingChannel
     *
     * @return The config model
     */
    public BroadcastingChannelModel getBroadcastingChannelModel() {
        return new BroadcastingChannelModel(
            projectSettingState,
            broadcastingChannelNameTextField.getText(),
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
        return broadcastingChannelNameTextField;
    }

    /**
     * Validates the dialog input
     *
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = broadcastingChannelNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", broadcastingChannelNameTextField);
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

        broadcastingChannelNameTextField = new JTextField();

        initDefaultContentPaneSettings();

        gbc.insets = JBUI.insetsLeft(3);
        contentPane.add(new JLabel("Enter Channel name:"), gbc);
        gbc.insets = JBUI.insetsLeft(0);
        gbc.insets = JBUI.insetsBottom(15);
        gbc.gridy++;
        contentPane.add(this.broadcastingChannelNameTextField, gbc);
        broadcastingChannelNameTextField.requestFocusInWindow();

        return contentPane;
    }
}

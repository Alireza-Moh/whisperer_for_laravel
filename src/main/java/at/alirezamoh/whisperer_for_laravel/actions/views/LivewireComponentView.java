package at.alirezamoh.whisperer_for_laravel.actions.views;

import at.alirezamoh.whisperer_for_laravel.actions.models.LivewireComponentClassModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.LivewireComponentViewModel;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class LivewireComponentView extends BaseDialog {
    /**
     * Text field for entering the component class name
     */
    private JTextField componentNameTextField;

    private JCheckBox inlineTextCheckBox;

    /**
     * @param project The current project
     */
    public LivewireComponentView(Project project) {
        super(project);

        setTitle("Create Livewire Component");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the Blade component class model
     * @return The Blade component class model
     */
    public LivewireComponentClassModel getLivewireComponentClassModel() {
        return new LivewireComponentClassModel(
            projectSettingState,
            componentNameTextField.getText(),
            getUnformattedModuleFullPath(moduleNameComboBox.getItem()),
            getSelectedFormattedModuleFullPath(),
            inlineTextCheckBox.isSelected()
        );
    }

    /**
     * Returns the Blade component view model
     * @return The Blade component view model
     */
    public LivewireComponentViewModel getLivewireComponentViewModel(LivewireComponentClassModel livewireComponentClassModel) {
        String unformattedModuleFullPath = getUnformattedModuleFullPath(moduleNameComboBox.getItem());
        if (!projectSettingState.isModuleApplication()) {
            unformattedModuleFullPath = "";
        }

        String name = StrUtils.removeDoubleForwardSlashes(
            livewireComponentClassModel.getFolderPath()
                + StrUtils.addSlashes(
                    StrUtils.lcFirst(livewireComponentClassModel.getName())
            )
        );

        return new LivewireComponentViewModel(
            projectSettingState,
            name,
            unformattedModuleFullPath,
            getSelectedFormattedModuleFullPath()
        );
    }

    /**
     * Creates the center panel of the dialog
     * @return The center panel
     */
    @Override
    protected @Nullable JComponent createCenterPanel() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());

        componentNameTextField = new JTextField();

        this.inlineTextCheckBox = new JCheckBox("Inline HTML");

        initDefaultContentPaneSettings();

        gbc.insets = JBUI.insetsLeft(3);
        contentPane.add(new JLabel("Component name"), gbc);
        gbc.gridy++;
        gbc.insets = JBUI.insetsLeft(0);
        gbc.insets = JBUI.insetsBottom(15);
        contentPane.add(componentNameTextField, gbc);

        gbc.gridy++;
        gbc.insets = JBUI.insetsLeft(0);
        gbc.insets = JBUI.insetsBottom(15);
        contentPane.add(inlineTextCheckBox, gbc);

        return contentPane;
    }

    /**
     * Returns the focused component when the dialog opens
     * @return The preferred focused component
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return componentNameTextField;
    }

    /**
     * Validates the dialog input
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = componentNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", componentNameTextField);
        }
        return null;
    }
}

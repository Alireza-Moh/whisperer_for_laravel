package at.alirezamoh.idea_whisperer_for_laravel.actions.views;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.BladeComponentClassModel;
import at.alirezamoh.idea_whisperer_for_laravel.actions.models.BladeComponentViewModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class BladeComponentView extends BaseDialog {
    /**
     * Text field for entering the component class name
     */
    private JTextField componentNameTextField;

    /**
     * Select input for selecting the component type
     */
    private ComboBox<String> componentTypeComboBox;

    /**
     * @param project The current project
     */
    public BladeComponentView(Project project) {
        super(project);

        setTitle("Create Blade Component / Blade File");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the Blade component class model
     * @return The Blade component class model
     */
    public BladeComponentClassModel getBladeComponentClassModel() {
        return new BladeComponentClassModel(
            componentNameTextField.getText(),
            getUnformattedModuleFullPath(moduleNameComboBox.getItem()),
            getSelectedFormattedModuleFullPath(),
            getModuleDirSrcName()
        );
    }

    /**
     * Returns the Blade component view model
     * @return The Blade component view model
     */
    public BladeComponentViewModel getBladeComponentViewModel() {
        String unformattedModuleFullPath = getUnformattedModuleFullPath(moduleNameComboBox.getItem());
        if (!projectSettingState.isModuleApplication()) {
            unformattedModuleFullPath = "";
        }

        return new BladeComponentViewModel(
            componentNameTextField.getText(),
            unformattedModuleFullPath,
            getSelectedFormattedModuleFullPath(),
            getModuleDirSrcName()
        );
    }

    /**
     * Checks if both the component class and view should be created
     * @return True if both should be created, false otherwise
     */
    public boolean withBladeComponentClassAndBladeView() {
        return componentTypeComboBox.getSelectedIndex() == 0;
    }

    /**
     * Checks if only the component class should be created
     * @return True if only the class should be created, false otherwise
     */
    public boolean onlyComponentClass() {
        return componentTypeComboBox.getSelectedIndex() == 2;
    }

    /**
     * Checks if only the component view should be created
     * @return True if only the view should be created, false otherwise
     */
    public boolean onlyComponentBladeView() {
        return componentTypeComboBox.getSelectedIndex() == 1;
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
        componentTypeComboBox = new ComboBox<>(
            new String[]{
                "Class and blade",
                "Only blade",
                "Only Class",
            }
        );

        initDefaultContentPaneSettings();

        gbc.insets = JBUI.insetsLeft(3);
        contentPane.add(new JLabel("Component name(without: 'x-')"), gbc);
        gbc.gridy++;
        gbc.insets = JBUI.insetsLeft(0);
        gbc.insets = JBUI.insetsBottom(15);
        contentPane.add(componentNameTextField, gbc);
        gbc.gridy++;

        gbc.insets = JBUI.insetsLeft(3);
        contentPane.add(new JLabel("Component Type"), gbc);
        gbc.gridy++;
        gbc.insets = JBUI.insetsLeft(0);
        gbc.insets = JBUI.insetsBottom(15);
        contentPane.add(componentTypeComboBox, gbc);

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

package at.alirezamoh.idea_whisperer_for_laravel.settings;

import com.intellij.ide.HelpTooltip;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsComponent {
    /**
     * The form builder factory
     */
    private final FormBuilder formBuilder;

    /**
     * Combo box to select the project type (Standard or Module-based)
     */
    private final ComboBox<String> projectTypeComboBox = new ComboBox<>(new String[]{"Standard Application", "Module based Application"});

    /**
     * Label for the root directory path
     */
    private final JBLabel moduleRootDirectoryPathLabel = new JBLabel("Module root directory path:");

    /**
     * Label for the module src directory name
     */
    private final JBLabel moduleSrcDirectoryLabel = new JBLabel("Module src directory name:");

    /**
     * Text field to input module src directory name for module-based projects
     */
    private final JBTextField moduleSrcDirectoryTextField = new JBTextField();

    /**
     * Text field to input the root directory path for module-based projects
     */
    private final JBTextField moduleRootDirectoryPathTextField = new JBTextField();

    /**
     * Text field to input the root app path
     */
    private final JBTextField rootAppPathTextField = new JBTextField();

    SettingsComponent() {
        formBuilder = FormBuilder.createFormBuilder();

        hideModuleTextFields();

        addModuleSettingsComponent();
        fillPanel();

        addEventListenerToProjectTypeComboBox();
    }

    /**
     * Returns main panel with the necessary components
     * @return main panel
     */
    public JPanel getPanel() {
        return formBuilder.getPanel();
    }

    public String getRootAppPath() {
        return rootAppPathTextField.getText();
    }

    public void setRootAppPathTextField(String newPath) {
        this.rootAppPathTextField.setText(newPath);
    }

    public String getModuleRootDirectoryPath() {
        return moduleRootDirectoryPathTextField.getText();
    }

    public void setModuleRootDirectoryPathTextField(String newPath) {
        this.moduleRootDirectoryPathTextField.setText(newPath);
    }

    public String getProjectType() {
        return projectTypeComboBox.getItem();
    }

    public void setProjectTypeComboBox(String selectedItem) {
        this.projectTypeComboBox.setSelectedItem(selectedItem);
    }

    public String getModuleSrcDirectoryName() {
        return moduleSrcDirectoryTextField.getText();
    }

    public void setModuleSrcDirectoryName(String srcDirName) {
        this.moduleSrcDirectoryTextField.setText(srcDirName);
    }

    private void hideModuleTextFields() {
        projectTypeComboBox.setPreferredSize(new java.awt.Dimension(300, projectTypeComboBox.getPreferredSize().height));

        moduleRootDirectoryPathLabel.setVisible(false);
        moduleRootDirectoryPathTextField.setVisible(false);

        moduleSrcDirectoryLabel.setVisible(false);
        moduleSrcDirectoryTextField.setVisible(false);

        rootAppPathTextField.setVisible(false);
    }

    /**
     * Adds the text fields for saving the based module data
     */
    private void addModuleSettingsComponent() {
        formBuilder.addLabeledComponent(
            new JBLabel("Project type:"),
            projectTypeComboBox,
            20,
            false
        )
        .addLabeledComponent(
            moduleRootDirectoryPathLabel,
            moduleRootDirectoryPathTextField,
            10,
            false
        )
        .addLabeledComponent(
            moduleSrcDirectoryLabel,
            moduleSrcDirectoryTextField,
            10,
            false
        );

        new HelpTooltip()
            .setLocation(HelpTooltip.Alignment.RIGHT)
            .setDescription("The main folder where all your project modules are located")
            .installOn(moduleRootDirectoryPathTextField);

        new HelpTooltip()
            .setLocation(HelpTooltip.Alignment.RIGHT)
            .setDescription("The folder name containing the core code for directories like (e.g., Controllers, Commands)")
            .installOn(moduleSrcDirectoryTextField);

        new HelpTooltip()
            .setLocation(HelpTooltip.Alignment.RIGHT)
            .setDescription("The folder path containing the primary code for your application default is app")
            .installOn(rootAppPathTextField);
    }

    private void fillPanel() {
        formBuilder.addComponentFillVertically(new JPanel(), 0);
    }

    private void addEventListenerToProjectTypeComboBox() {
        projectTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedItem = (String) projectTypeComboBox.getSelectedItem();
                boolean isSimpleDirectoryModule = "Module based Application".equals(selectedItem);

                moduleRootDirectoryPathLabel.setVisible(isSimpleDirectoryModule);
                moduleRootDirectoryPathTextField.setVisible(isSimpleDirectoryModule);
                moduleRootDirectoryPathTextField.setText("/Modules/");
                moduleRootDirectoryPathTextField.setText("/app/");

                rootAppPathTextField.setVisible(isSimpleDirectoryModule);

                moduleSrcDirectoryTextField.setText("app");
                moduleSrcDirectoryLabel.setVisible(isSimpleDirectoryModule);
                moduleSrcDirectoryTextField.setVisible(isSimpleDirectoryModule);
            }
        });
    }

}

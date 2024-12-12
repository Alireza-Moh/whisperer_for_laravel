package at.alirezamoh.idea_whisperer_for_laravel.settings;

import com.intellij.ide.HelpTooltip;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsComponent {
//    /**
//     * The form builder factory
//     */
//    private final FormBuilder formBuilder;
//
//    /**
//     * Combo box to select the project type (Standard or Module-based)
//     */
//    private final ComboBox<String> projectTypeComboBox = new ComboBox<>(new String[]{"Standard Application", "Module based Application"});
//
//    /**
//     * Label for the root directory path
//     */
//    private final JBLabel moduleRootDirectoryPathLabel = new JBLabel("Module root directory path:");
//
//    /**
//     * Text field to input the root directory path for module-based projects
//     */
//    private final JBTextField moduleRootDirectoryPathTextField = new JBTextField();
//
//    SettingsComponent() {
//        formBuilder = FormBuilder.createFormBuilder();
//
//        hideModuleTextFields();
//
//        addModuleSettingsComponent();
//        fillPanel();
//
//        addEventListenerToProjectTypeComboBox();
//    }
//
//    /**
//     * Returns main panel with the necessary components
//     * @return main panel
//     */
//    public JPanel getPanel() {
//        return formBuilder.getPanel();
//    }
//
//    public String getModuleRootDirectoryPath() {
//        return moduleRootDirectoryPathTextField.getText();
//    }
//
//    public void setModuleRootDirectoryPathTextField(String newPath) {
//        this.moduleRootDirectoryPathTextField.setText(newPath);
//    }
//
//    public String getProjectType() {
//        return projectTypeComboBox.getItem();
//    }
//
//    public void setProjectTypeComboBox(String selectedItem) {
//        this.projectTypeComboBox.setSelectedItem(selectedItem);
//    }
//
//    private void hideModuleTextFields() {
//        projectTypeComboBox.setPreferredSize(new java.awt.Dimension(300, projectTypeComboBox.getPreferredSize().height));
//
//        moduleRootDirectoryPathLabel.setVisible(false);
//        moduleRootDirectoryPathTextField.setVisible(false);
//    }
//
//    /**
//     * Adds the text fields for saving the based module data
//     */
//    private void addModuleSettingsComponent() {
//        formBuilder.addLabeledComponent(
//            new JBLabel("Project type:"),
//            projectTypeComboBox,
//            20,
//            false
//        )
//        .addLabeledComponent(
//            moduleRootDirectoryPathLabel,
//            moduleRootDirectoryPathTextField,
//            10,
//            false
//        );
//
//        new HelpTooltip()
//            .setLocation(HelpTooltip.Alignment.RIGHT)
//            .setDescription("The main folder where all your project modules are located")
//            .installOn(moduleRootDirectoryPathTextField);
//    }
//
//    private void fillPanel() {
//        formBuilder.addComponentFillVertically(new JPanel(), 0);
//    }
//
//    private void addEventListenerToProjectTypeComboBox() {
//        projectTypeComboBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                String selectedItem = (String) projectTypeComboBox.getSelectedItem();
//                boolean isSimpleDirectoryModule = "Module based Application".equals(selectedItem);
//
//                moduleRootDirectoryPathLabel.setVisible(isSimpleDirectoryModule);
//                moduleRootDirectoryPathTextField.setVisible(isSimpleDirectoryModule);
//                moduleRootDirectoryPathTextField.setText("/Modules/");
//                moduleRootDirectoryPathTextField.setText("/app/");
//            }
//        });
//    }

    private final JBTabbedPane tabbedPane = new JBTabbedPane();


    /*==============Components for General Settings==============*/
    private final JBTextField projectRootDirectoryTextField = new JBTextField();
    private final ComboBox<String> projectTypeComboBox = new ComboBox<>(new String[]{"Standard Application", "Module based Application"});
    private final JBTextField modulesDirectoryPathTextField = new JBTextField();
    private final JBLabel modulesDirectoryPathLabel = new JBLabel("Modules directory path:");
    private final JBTextField moduleSrcDirectoryPathTextField = new JBTextField();
    private final JBLabel moduleSrcDirectoryPathLabel = new JBLabel("Module src directory path:");


    public SettingsComponent(Project project) {
        initializeTabs();

        updateModuleSettingsVisibility();
        addEventListenerToProjectTypeComboBox();
    }

    /**
     * Returns the main panel containing the tabbed pane
     *
     * @return JPanel
     */
    public JPanel getPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.NORTH);
        return mainPanel;
    }

    public String getProjectRootDirectoryPath() {
        return projectRootDirectoryTextField.getText();
    }

    public void setProjectRootDirectoryPath(String newPath) {
        this.projectRootDirectoryTextField.setText(newPath);
    }

    public String getProjectType() {
        return projectTypeComboBox.getItem();
    }

    public void setProjectTypeComboBox(String selectedItem) {
        this.projectTypeComboBox.setSelectedItem(selectedItem);
    }

    public String getModulesDirectoryPath() {
        return modulesDirectoryPathTextField.getText();
    }

    public void setModulesDirectoryPathTextField(String newPath) {
        this.modulesDirectoryPathTextField.setText(newPath);
    }

    public String getModuleSrcDirectoryPath() {
        return moduleSrcDirectoryPathTextField.getText();
    }

    public void setModuleSrcDirectoryPathTextField(String newPath) {
        this.moduleSrcDirectoryPathTextField.setText(newPath);
    }

    /**
     * Initializes the tabs in the `JBTabbedPane`
     */
    private void initializeTabs() {
        tabbedPane.addTab("General Settings", createGeneralSettingsPanel());
    }

    /**
     * Creates the General Settings tab
     *
     * @return JPanel
     */
    private JPanel createGeneralSettingsPanel() {
        FormBuilder builder = FormBuilder.createFormBuilder()
            .addLabeledComponent(new JBLabel("Laravel directory:"), projectRootDirectoryTextField, 10, false)
            .addLabeledComponent(new JBLabel("Project type:"), createComboBoxPanel(projectTypeComboBox), 10, false)
            .addLabeledComponent(modulesDirectoryPathLabel, modulesDirectoryPathTextField, 10, false)
            .addLabeledComponent(moduleSrcDirectoryPathLabel, moduleSrcDirectoryPathTextField, 10, false);

        new HelpTooltip()
            .setLocation(HelpTooltip.Alignment.RIGHT)
            .setDescription("The main folder path where all your modules are located")
            .installOn(modulesDirectoryPathTextField);

        new HelpTooltip()
            .setLocation(HelpTooltip.Alignment.RIGHT)
            .setDescription("The folder path containing the core code for directories like (e.g., Controllers, Models) in each module")
            .installOn(moduleSrcDirectoryPathTextField);

        return builder.getPanel();
    }

    private JPanel createComboBoxPanel(ComboBox<String> comboBox) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(comboBox, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Updates the visibility of the Module Root Directory field based on the project type
     */
    private void updateModuleSettingsVisibility() {
        boolean isModuleBased = "Module based Application".equals(projectTypeComboBox.getSelectedItem());
        modulesDirectoryPathLabel.setVisible(isModuleBased);
        modulesDirectoryPathTextField.setVisible(isModuleBased);
    }

    private void addEventListenerToProjectTypeComboBox() {
        projectTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedItem = (String) projectTypeComboBox.getSelectedItem();
                boolean isSimpleDirectoryModule = "Module based Application".equals(selectedItem);

                modulesDirectoryPathLabel.setVisible(isSimpleDirectoryModule);
                modulesDirectoryPathTextField.setVisible(isSimpleDirectoryModule);
                moduleSrcDirectoryPathLabel.setVisible(isSimpleDirectoryModule);
                moduleSrcDirectoryPathTextField.setVisible(isSimpleDirectoryModule);

                if (isSimpleDirectoryModule) {
                    modulesDirectoryPathTextField.setText("Modules");
                    moduleSrcDirectoryPathTextField.setText("app");
                }
            }
        });
    }
}

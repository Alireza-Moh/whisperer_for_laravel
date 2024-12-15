package at.alirezamoh.idea_whisperer_for_laravel.settings;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.*;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class SettingsComponent {
    private final JBTabbedPane tabbedPane = new JBTabbedPane();

    /*==============Components for General Settings==============*/
    private final JPanel laravelDirectoryPanel = new JPanel(new BorderLayout());
    private final JPanel modulesDirectoryPanel = new JPanel(new BorderLayout());
    private final JPanel moduleSrcDirectoryPanel = new JPanel(new BorderLayout());
    private final JBTextField laravelDirectoryTextField = new JBTextField();
    private final ComboBox<String> projectTypeComboBox = new ComboBox<>(new String[]{"Standard Application", "Module based Application"});
    private final JBTextField modulesDirectoryPathTextField = new JBTextField();
    private final JBLabel modulesDirectoryPathLabel = new JBLabel("Root directory:");
    private final JBTextField moduleSrcDirectoryPathTextField = new JBTextField();
    private final JBLabel moduleSrcDirectoryPathLabel = new JBLabel("Module source:");


    /*==============Components for laravel packages==============*/
    private final JBTextField inertiaPageComponentRootPathTextField = new JBTextField();


    public SettingsComponent() {
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
        return laravelDirectoryTextField.getText();
    }

    public void setProjectRootDirectoryPath(String newPath) {
        this.laravelDirectoryTextField.setText(newPath);
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

    public String getInertiaPageComponentRootPath() {
        return inertiaPageComponentRootPathTextField.getText();
    }

    public void setInertiaPageComponentRootPath(String inertiaPageComponentRootPath) {
        this.inertiaPageComponentRootPathTextField.setText(inertiaPageComponentRootPath);
    }

    /**
     * Initializes the tabs in the `JBTabbedPane`
     */
    private void initializeTabs() {
        tabbedPane.addTab("General Settings", createGeneralSettingsPanel());
        tabbedPane.addTab("Laravel packages", createLaravelPackagesPanel());
    }

    /**
     * Creates the General Settings tab
     *
     * @return JPanel
     */
    private JPanel createGeneralSettingsPanel() {
        laravelDirectoryPanel.add(laravelDirectoryTextField, BorderLayout.NORTH);
        JBLabel laravelDirectoryHintLabel = new JBLabel("Leave blank if the Laravel project is in the root directory.");
        laravelDirectoryHintLabel.setFont(laravelDirectoryHintLabel.getFont().deriveFont(Font.ITALIC));
        laravelDirectoryHintLabel.setForeground(JBColor.GRAY);
        laravelDirectoryPanel.add(laravelDirectoryHintLabel, BorderLayout.CENTER);

        modulesDirectoryPanel.add(modulesDirectoryPathTextField, BorderLayout.NORTH);
        JBLabel modulesDirectoryHintLabel = new JBLabel("The main folder path for all modules");
        modulesDirectoryHintLabel.setFont(modulesDirectoryHintLabel.getFont().deriveFont(Font.ITALIC));
        modulesDirectoryHintLabel.setForeground(JBColor.GRAY);
        modulesDirectoryPanel.add(modulesDirectoryHintLabel, BorderLayout.CENTER);

        moduleSrcDirectoryPanel.add(moduleSrcDirectoryPathTextField, BorderLayout.NORTH);
        JBLabel moduleSrcDirectoryHintLabel = new JBLabel("Leave blank if the source directory is app");
        moduleSrcDirectoryHintLabel.setFont(moduleSrcDirectoryHintLabel.getFont().deriveFont(Font.ITALIC));
        moduleSrcDirectoryHintLabel.setForeground(JBColor.GRAY);
        moduleSrcDirectoryPanel.add(moduleSrcDirectoryHintLabel, BorderLayout.CENTER);

        FormBuilder builder = FormBuilder.createFormBuilder()
            .addLabeledComponent(new JBLabel("Laravel directory:"), laravelDirectoryPanel, 10, false)
            .addLabeledComponent(new JBLabel("Project type:"), createComboBoxPanel(projectTypeComboBox), 10, false)
            .addLabeledComponent(modulesDirectoryPathLabel, modulesDirectoryPanel, 10, false)
            .addLabeledComponent(moduleSrcDirectoryPathLabel, moduleSrcDirectoryPanel, 10, false);

        return builder.getPanel();
    }

    private JPanel createLaravelPackagesPanel() {
        inertiaPageComponentRootPathTextField.setEditable(false); // Read-only to encourage use of the popup
        inertiaPageComponentRootPathTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openPathsManagerDialog();
            }
        });
        FormBuilder builder = FormBuilder.createFormBuilder()
            .addLabeledComponent(
                new JBLabel("Inertia Page component paths:"),
                inertiaPageComponentRootPathTextField,
                10,
                false
            );

        JPanel parentPanel = new JPanel(new BorderLayout());
        parentPanel.add(builder.getPanel(), BorderLayout.PAGE_START);

        return parentPanel;
    }

    private void openPathsManagerDialog() {
        InertiaPathsDialog dialog = new InertiaPathsDialog(inertiaPageComponentRootPathTextField);

        dialog.show();
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

                modulesDirectoryPanel.setVisible(isSimpleDirectoryModule);
                moduleSrcDirectoryPanel.setVisible(isSimpleDirectoryModule);

                if (isSimpleDirectoryModule) {
                    modulesDirectoryPathTextField.setText("Modules");
                    moduleSrcDirectoryPathTextField.setText("app");
                }
            }
        });
    }
}

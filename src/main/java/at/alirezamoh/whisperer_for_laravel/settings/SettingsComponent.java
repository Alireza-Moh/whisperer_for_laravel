package at.alirezamoh.whisperer_for_laravel.settings;

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

public class SettingsComponent {
    private final JBTabbedPane tabbedPane = new JBTabbedPane();

    /*==============Components for General Settings==============*/
    private final JPanel projectDirectoryPanel = new JPanel(new BorderLayout());
    private final JPanel modulesDirectoryPanel = new JPanel(new BorderLayout());
    private final JPanel moduleSrcDirectoryPanel = new JPanel(new BorderLayout());
    private final JBTextField projectDirectoryTextField = new JBTextField();
    private final ComboBox<String> projectTypeComboBox = new ComboBox<>(new String[]{"Standard Application", "Module based Application"});
    private final JBTextField modulesDirectoryPathTextField = new JBTextField();
    private final JBLabel modulesDirectoryPathLabel = new JBLabel("Root directory:");
    private final JBTextField moduleSrcDirectoryPathTextField = new JBTextField();
    private final JBLabel moduleSrcDirectoryPathLabel = new JBLabel("Module source:");


    /*==============Components for laravel packages==============*/
    private final JBTextField inertiaPageRootPathTextField = new JBTextField();


    /*==============Components for laravel inspections & suppressions==============*/
    private final JBCheckBox suppressRealTimeFacadeWarningsCheckbox = new JBCheckBox();
    private final JBCheckBox routeNotFoundAnnotatorWarningCheckbox = new JBCheckBox();

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
        return projectDirectoryTextField.getText();
    }

    public void setProjectRootDirectoryPath(String newPath) {
        this.projectDirectoryTextField.setText(newPath);
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

    public String getInertiaPageRootPath() {
        return inertiaPageRootPathTextField.getText();
    }

    public void setInertiaPageRootPath(String inertiaPageRootPath) {
        this.inertiaPageRootPathTextField.setText(inertiaPageRootPath);
    }

    public boolean suppressRealTimeFacadeWarnings() {
        return suppressRealTimeFacadeWarningsCheckbox.isSelected();
    }

    public void setSuppressRealTimeFacadeWarnings(boolean suppress) {
        this.suppressRealTimeFacadeWarningsCheckbox.setSelected(suppress);
    }

    public boolean getRouteNotFoundAnnotatorWarningCheckbox() {
        return routeNotFoundAnnotatorWarningCheckbox.isSelected();
    }

    public void setRouteNotFoundAnnotatorWarningCheckbox(boolean suppress) {
        this.routeNotFoundAnnotatorWarningCheckbox.setSelected(suppress);
    }

    /**
     * Initializes the tabs in the `JBTabbedPane`
     */
    private void initializeTabs() {
        tabbedPane.addTab("General Settings", createGeneralSettingsPanel());
        tabbedPane.addTab("Settings for laravel packages", createLaravelPackagesPanel());
        tabbedPane.addTab("Inspections & Suppressions", createInspectionsAndSuppressionsPanel());
    }

    /**
     * Creates the General Settings tab
     *
     * @return JPanel
     */
    private JPanel createGeneralSettingsPanel() {
        projectDirectoryPanel.add(projectDirectoryTextField, BorderLayout.NORTH);
        JBLabel laravelDirectoryHintLabel = new JBLabel("Leave blank if your project is located in the root directory");
        laravelDirectoryHintLabel.setFont(laravelDirectoryHintLabel.getFont().deriveFont(Font.ITALIC));
        laravelDirectoryHintLabel.setForeground(JBColor.GRAY);
        projectDirectoryPanel.add(laravelDirectoryHintLabel, BorderLayout.CENTER);

        modulesDirectoryPanel.add(modulesDirectoryPathTextField, BorderLayout.NORTH);
        JBLabel modulesDirectoryHintLabel = new JBLabel("The main folder path for all modules");
        modulesDirectoryHintLabel.setFont(modulesDirectoryHintLabel.getFont().deriveFont(Font.ITALIC));
        modulesDirectoryHintLabel.setForeground(JBColor.GRAY);
        modulesDirectoryPanel.add(modulesDirectoryHintLabel, BorderLayout.CENTER);

        moduleSrcDirectoryPanel.add(moduleSrcDirectoryPathTextField, BorderLayout.NORTH);
        JBLabel moduleSrcDirectoryHintLabel = new JBLabel("Leave blank if the source directory of each module is app");
        moduleSrcDirectoryHintLabel.setFont(moduleSrcDirectoryHintLabel.getFont().deriveFont(Font.ITALIC));
        moduleSrcDirectoryHintLabel.setForeground(JBColor.GRAY);
        moduleSrcDirectoryPanel.add(moduleSrcDirectoryHintLabel, BorderLayout.CENTER);

        FormBuilder builder = FormBuilder.createFormBuilder()
            .addLabeledComponent(new JBLabel("Project directory:"), projectDirectoryPanel, 10, false)
            .addLabeledComponent(new JBLabel("Project type:"), createComboBoxPanel(projectTypeComboBox), 10, false)
            .addLabeledComponent(modulesDirectoryPathLabel, modulesDirectoryPanel, 10, false)
            .addLabeledComponent(moduleSrcDirectoryPathLabel, moduleSrcDirectoryPanel, 10, false);

        return builder.getPanel();
    }

    private JPanel createLaravelPackagesPanel() {
        inertiaPageRootPathTextField.setEditable(false);
        inertiaPageRootPathTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openPathsManagerDialog();
            }
        });
        FormBuilder builder = FormBuilder.createFormBuilder()
            .addLabeledComponent(
                new JBLabel("All inertia page paths:"),
                inertiaPageRootPathTextField,
                10,
                false
            );

        JPanel parentPanel = new JPanel(new BorderLayout());
        parentPanel.add(builder.getPanel(), BorderLayout.PAGE_START);

        return parentPanel;
    }

    private JPanel createInspectionsAndSuppressionsPanel() {
        JPanel checkboxPanel = new JPanel(new GridLayout(0, 1));

        JPanel suppressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        suppressPanel.add(suppressRealTimeFacadeWarningsCheckbox);
        suppressPanel.add(new JBLabel("Suppress real-time facade warnings: 'Undefined class' & 'Undefined namespace'"));
        checkboxPanel.add(suppressPanel);

        JPanel routePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        routePanel.add(routeNotFoundAnnotatorWarningCheckbox);
        routePanel.add(new JBLabel("Annotate undefined route names: 'Route name not found'"));
        checkboxPanel.add(routePanel);

        FormBuilder builder = FormBuilder.createFormBuilder()
            .addComponent(checkboxPanel);

        JPanel parentPanel = new JPanel(new BorderLayout());
        parentPanel.add(builder.getPanel(), BorderLayout.PAGE_START);

        return parentPanel;
    }

    private void openPathsManagerDialog() {
        InertiaPathsDialog dialog = new InertiaPathsDialog(inertiaPageRootPathTextField);

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

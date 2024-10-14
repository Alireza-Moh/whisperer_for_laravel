package at.alirezamoh.idea_whisperer_for_laravel.actions.views;

import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.notification.Notify;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiDirectory;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for dialogs in the plugin
 */
public abstract class BaseDialog extends DialogWrapper {
    /**
     * The main content panel of the dialog
     */
    protected JPanel contentPane;

    /**
     * The current project
     */
    protected Project project;

    /**
     * The project settings
     */
    protected SettingsState projectSettingState;

    /**
     * Flag indicating if the project is a module-based application
     */
    protected boolean isAModuleApplication;

    /**
     * The root path of the modules
     */
    protected String moduleRootPath;

    /**
     * Map of module paths (unformatted and formatted)
     */
    protected Map<String, String> modules = new HashMap<>();

    /**
     * Select input for the modules
     */
    protected ComboBox<String> moduleNameComboBox;

    /**
     * GridBagConstraints for layout management
     */
    protected GridBagConstraints gbc;

    /**
     * @param project The current project
     */
    protected BaseDialog(Project project) {
        super(project);

        this.project = project;
        this.gbc = new GridBagConstraints();

        this.initProjectModuleSettings();
        this.initModuleList();
    }

    /**
     * Returns the unformatted module full path based on the formatted one
     * @param formattedModuleFullPath The formatted module full path
     * @return The unformatted module full path
     */
    protected String getUnformattedModuleFullPath(String formattedModuleFullPath) {
        if (formattedModuleFullPath == null) {
            return "app";
        }

        String unformattedPath = null;

        for (Map.Entry<String, String> entry : modules.entrySet()) {
            if (entry.getValue().equals(formattedModuleFullPath)) {
                unformattedPath = entry.getKey();
                break;
            }
        }

        return unformattedPath;
    }

    protected String getUnformattedModuleFullPathForNoneAppDir() {
        String unformattedModuleFullPath = getUnformattedModuleFullPath(moduleNameComboBox.getItem());
        if (!projectSettingState.isModuleApplication()) {
            unformattedModuleFullPath = "";
        }

        return unformattedModuleFullPath;
    }

    /**
     * Initializes the default content pane settings
     * It adds the module select input to the main panel
     */
    protected void initDefaultContentPaneSettings() {
       moduleNameComboBox = new ComboBox<>(this.modules.values().toArray(new String[0]));

       gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

       gbc.gridx = 0;
       gbc.gridy = 0;

        if (this.isAModuleApplication) {
           gbc.insets = JBUI.insetsLeft(3);
           contentPane.add(new JLabel("Module"),gbc);
           gbc.gridy++;
            gbc.insets = JBUI.insetsLeft(0);
           gbc.insets = JBUI.insetsBottom(15);
           contentPane.add(this.moduleNameComboBox,gbc);
           gbc.gridy++;
        }
    }

    /**
     * Returns the selected formatted module full path from the select input
     * @return The selected formatted module full path
     */
    protected String getSelectedFormattedModuleFullPath() {
        String selected = moduleNameComboBox.getItem();

        if (selected == null) {
            return "App";
        }

        if (selected.equals("App root path")) {
            return "App";
        }
        else {
            return selected;
        }
    }
    
    protected String getModuleDirSrcName() {
        String selected = moduleNameComboBox.getItem();

        if (selected == null || getUnformattedModuleFullPath(selected).equals("/app")) {
            return "";
        }
        return projectSettingState.getModuleSrcDirectoryName();
    }

    /**
     * Initializes the project module settings
     */
    private void initProjectModuleSettings() {
       projectSettingState = SettingsState.getInstance(this.project);

       isAModuleApplication = projectSettingState.isModuleApplication();
       moduleRootPath = projectSettingState.replaceAndSlashes(this.projectSettingState.getModuleRootDirectoryPath());
    }

    /**
     * Initializes the list of modules
     */
    private void initModuleList() {
        if (!this.isAModuleApplication) {
            return;
        }

        PsiDirectory rootDir = DirectoryPsiUtil.getDirectory(project,moduleRootPath);
        if (rootDir != null) {
           modules.put("/app", "App root path");
            for (PsiDirectory module : rootDir.getSubdirectories()) {
               formattedModulePath(module.getName());
            }
        }
        else {
            Notify.notifyError(
               project,
                "Module root path [" +moduleRootPath + "] not found"
            );

           isAModuleApplication = false;
            getOKAction().setEnabled(false);
        }
    }

    /**
     * Formats the module path for display in the select input
     * @param modulePath The module path to format
     */
    private void formattedModulePath(String modulePath) {
        String unformattedModuleFullPath = moduleRootPath + modulePath;
        String backSlashedModuleFullPath = unformattedModuleFullPath.replace("/", "\\");

        StringBuilder result = new StringBuilder();

        List<String> paths = Arrays.stream(backSlashedModuleFullPath.split("\\\\"))
            .filter(s -> !s.isEmpty())
            .toList();

        for (String word : paths) {
            result
                .append(
                    Character.toUpperCase(word.charAt(0))
                )
                .append(
                    word.substring(1)
                )
                .append("\\");
        }

        if (!result.isEmpty()) {
            result.deleteCharAt(result.length() - 1);
        }

       modules.put(unformattedModuleFullPath.replace("\\", "/"), result.toString());
    }
}

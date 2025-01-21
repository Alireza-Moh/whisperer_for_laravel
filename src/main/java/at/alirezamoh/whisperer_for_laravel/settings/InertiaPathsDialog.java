package at.alirezamoh.whisperer_for_laravel.settings;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InertiaPathsDialog extends DialogWrapper {
    private final List<String> inertiaPagePaths = new ArrayList<>();

    private final JBTextField inertiaPathsField;

    private final JBList<String> pathsList;

    private final DefaultActionGroup actionGroup;

    public InertiaPathsDialog(JBTextField inertiaPathsField) {
        super(true);

        this.inertiaPathsField = inertiaPathsField;
        convertPathsToList();

        pathsList = new JBList<>(inertiaPagePaths.toArray(new String[0]));
        pathsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        actionGroup = new DefaultActionGroup();
        actionGroup.add(new AddPathAction());
        actionGroup.add(new EditPathAction());
        actionGroup.add(new RemovePathAction());

        setTitle("Manage Inertia Paths");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("InertiaPathDialog", actionGroup, true);
        toolbar.setTargetComponent(inertiaPathsField);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(new JScrollPane(pathsList), BorderLayout.CENTER);
        mainPanel.add(toolbar.getComponent(), BorderLayout.NORTH);

        return mainPanel;
    }

    @Override
    protected void doOKAction() {
        inertiaPathsField.setText(String.join(";", inertiaPagePaths));
        super.doOKAction();
    }

    private void updatePathsList() {
        pathsList.setListData(inertiaPagePaths.toArray(new String[0]));
    }

    private class AddPathAction extends AnAction {
        public AddPathAction() {
            super("Add Path", "Add a new Inertia path", AllIcons.General.Add);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            String selectedPath = pathsList.getSelectedValue();
            String newPath = Messages.showInputDialog(
                "Enter a new path",
                "New Path",
                AllIcons.Ide.ConfigFile,
                selectedPath,
                null
            );
            if (newPath != null && !newPath.trim().isEmpty()) {
                inertiaPagePaths.add(newPath.trim());
                updatePathsList();
            }
        }
    }

    private class EditPathAction extends AnAction {
        public EditPathAction() {
            super("Edit Path", "Edit an existing Inertia path", AllIcons.Actions.Edit);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            String selectedPath = pathsList.getSelectedValue();
            if (selectedPath != null) {
                String newPath = Messages.showInputDialog(
                    "Edit path",
                    "New Path",
                    AllIcons.Ide.ConfigFile,
                    selectedPath,
                    null
                );
                if (newPath != null && !newPath.trim().isEmpty()) {
                    inertiaPagePaths.set(pathsList.getSelectedIndex(), newPath.trim());
                    updatePathsList();
                }
            }
        }
    }

    private class RemovePathAction extends AnAction {
        public RemovePathAction() {
            super("Remove Path", "Remove a selected Inertia path", AllIcons.General.Remove);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            int selectedIndex = pathsList.getSelectedIndex();
            if (selectedIndex != -1) {
                inertiaPagePaths.remove(selectedIndex);
                updatePathsList();
            }
        }
    }

    private void convertPathsToList() {
        String[] path = inertiaPathsField.getText().split(";");

        inertiaPagePaths.addAll(Arrays.asList(path));
    }
}

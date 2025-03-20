package at.alirezamoh.whisperer_for_laravel.actions.views;

import at.alirezamoh.whisperer_for_laravel.actions.models.InertiaPageModel;
import at.alirezamoh.whisperer_for_laravel.packages.inertia.InertiaUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class InertiaPageView extends BaseDialog {
    /**
     * Text field for entering the inertia page name
     */
    private JTextField inertiaPageNameTextField;

    /**
     * Select the destination path
     */
    protected ComboBox<String> resourcePageDirectoryComboBox;

    /**
     * Select the page variant
     */
    protected ComboBox<String> pageVariantComboBox;

    /**
     * Select the page type
     */
    protected ComboBox<String> pageTypeComboBox;

    /**
     * @param project The current project
     */
    public InertiaPageView(Project project) {
        super(project);

        setTitle("Create Inertia Page");
        setSize(500, 200);
        setResizable(false);
        init();
    }

    /**
     * Returns the Inertia page model representing an inertia page
     * @return The inertia page model
     */
    public InertiaPageModel getInertiaPageModel() {
        return new InertiaPageModel(
            projectSettingState,
            inertiaPageNameTextField.getText(),
            "",
            "",
            resourcePageDirectoryComboBox.getItem(),
            Objects.equals(pageVariantComboBox.getItem(), "Options API"),
            pageTypeComboBox.getItem()
        );
    }

    /**
     * Returns the focused component when the dialog opens
     * @return The preferred focused component
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return inertiaPageNameTextField;
    }

    /**
     * Validates the dialog input
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = inertiaPageNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", inertiaPageNameTextField);
        }
        return null;
    }

    /**
     * Creates the center panel of the dialog
     * @return The center panel
     */
    @Override
    protected JComponent createCenterPanel() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());

        inertiaPageNameTextField = new JTextField();
        resourcePageDirectoryComboBox = new ComboBox<>(InertiaUtil.getInertiaPaths(project).toArray(new String[0]));
        pageVariantComboBox = new ComboBox<>(new String[]{"Options API", "Composition API"});
        pageTypeComboBox = new ComboBox<>(new String[]{".vue", ".jsx"});

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.insets = JBUI.insetsLeft(3);
        contentPane.add(new JLabel("Enter page name:"), gbc);
        gbc.insets = JBUI.insetsLeft(0);
        gbc.insets = JBUI.insetsBottom(15);
        gbc.gridy++;
        contentPane.add(inertiaPageNameTextField, gbc);
        inertiaPageNameTextField.requestFocusInWindow();

        gbc.gridy++;
        gbc.insets = JBUI.insetsLeft(3);
        contentPane.add(new JLabel("Page type:"), gbc);
        gbc.insets = JBUI.insetsLeft(0);
        gbc.insets = JBUI.insetsBottom(15);
        gbc.gridy++;
        contentPane.add(pageTypeComboBox, gbc);

        gbc.gridy++;
        gbc.insets = JBUI.insetsLeft(3);
        contentPane.add(new JLabel("Page variant: (Ignore if it's not a Vue file)"), gbc);
        gbc.insets = JBUI.insetsLeft(0);
        gbc.insets = JBUI.insetsBottom(15);
        gbc.gridy++;
        contentPane.add(pageVariantComboBox, gbc);

        gbc.gridy++;
        this.gbc.insets = JBUI.insetsLeft(3);
        this.contentPane.add(new JLabel("Page resource directory:"), this.gbc);
        this.gbc.gridy++;
        this.gbc.insets = JBUI.insetsLeft(0);
        this.gbc.insets = JBUI.insetsBottom(15);
        this.contentPane.add(resourcePageDirectoryComboBox, this.gbc);

        return contentPane;
    }
}

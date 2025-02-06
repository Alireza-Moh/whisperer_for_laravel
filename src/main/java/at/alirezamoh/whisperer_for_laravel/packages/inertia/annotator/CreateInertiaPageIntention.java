package at.alirezamoh.whisperer_for_laravel.packages.inertia.annotator;

import at.alirezamoh.whisperer_for_laravel.actions.models.InertiaPageModel;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.TemplateLoader;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Intention action to create a new Inertia page
 */
public class CreateInertiaPageIntention extends BaseIntentionAction {
    /**
     * The raw page path where the new page will be created
     */
    private final String pagePath;

    /**
     * The project settings
     */
    private SettingsState settingsState;

    /**
     * @param pagePath The target page path
     */
    public CreateInertiaPageIntention(String pagePath) {
        this.pagePath = StrUtils.removeQuotes(pagePath);
    }


    /**
     * Determines if this intention action is available
     * In this case, the action is always available
     *
     * @param project The current project
     * @param editor  The editor instance
     * @param psiFile The current PSI file
     * @return true or false
     */
    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        return true;
    }

    /**
     * Execute the intention action
     *
     * @param project The current project.
     * @param editor  The editor instance.
     * @param psiFile The current PSI file.
     * @throws IncorrectOperationException If the operation cannot be completed
     */
    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        List<String> options = getInertiaPaths(project);
        if (options == null) {
            return;
        }

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel comboPanel = new JPanel();
        comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.Y_AXIS));

        ComboBox<String> pathsComboBox = new ComboBox<>(options.toArray(new String[0]));
        pathsComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        ComboBox<String> pageVariantComboBox = new ComboBox<>(new String[]{"Options API", "Composition API"});
        pageVariantComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        ComboBox<String> pageTypeComboBox = new ComboBox<>(new String[]{".vue", ".jsx"});
        pageTypeComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);


        JLabel destinationLabel = new JLabel("Destination directory:");
        destinationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboPanel.add(destinationLabel);
        comboPanel.add(pathsComboBox);
        comboPanel.add(Box.createVerticalStrut(10));

        JLabel pageTypeLabel = new JLabel("Page type:");
        pageTypeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboPanel.add(pageTypeLabel);
        comboPanel.add(pageTypeComboBox);
        comboPanel.add(Box.createVerticalStrut(10));

        JLabel pageVariantLabel = new JLabel("Page variant:");
        pageVariantLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboPanel.add(pageVariantLabel);
        comboPanel.add(pageVariantComboBox);

        contentPanel.add(comboPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton createButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");
        createButton.setForeground(JBColor.blue);
        cancelButton.setForeground(JBColor.gray);
        buttonsPanel.add(createButton);
        buttonsPanel.add(cancelButton);
        contentPanel.add(buttonsPanel, BorderLayout.SOUTH);

        JBPopup popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(contentPanel, comboPanel)
            .setTitle("Create Inertia Page")
            .setResizable(false)
            .setMovable(true)
            .setRequestFocus(true)
            .setCancelButton(new IconButton(null, null))
            .createPopup();

        createButton.addActionListener(e -> {
            TemplateLoader templateProcessor = new TemplateLoader(
                project,
                "inertiaPage.ftl",
                new InertiaPageModel(
                    SettingsState.getInstance(project),
                    pagePath,
                    "",
                    "",
                    pathsComboBox.getItem(),
                    pageVariantComboBox.getItem().equals("Options API"),
                    pageTypeComboBox.getItem()
                )
            );

            WriteCommandAction.runWriteCommandAction(project, () -> {
                templateProcessor.createTemplateWithDirectory(true);
            });

            popup.closeOk(null);
        });
        cancelButton.addActionListener(e -> popup.cancel());

        popup.showInBestPositionFor(editor);
    }

    /**
     * Retrieves the available Inertia paths from the project settings
     *
     * @param project The current project
     * @return A list of inertia paths or an empty list if none are set
     */
    private List<String> getInertiaPaths(@NotNull Project project) {
        settingsState = SettingsState.getInstance(project);
        String inertiaPaths = settingsState.getInertiaPageRootPath();
        if (inertiaPaths == null) {
            return new ArrayList<>();
        }

        String[] paths = inertiaPaths.split(";");
        return Arrays.stream(paths)
            .filter(path -> !path.isEmpty())
            .toList();
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Create inertia page";
    }

    @Override
    public @NotNull @IntentionName String getText() {
        return "Create inertia page";
    }
}

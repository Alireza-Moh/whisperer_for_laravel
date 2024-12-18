package at.alirezamoh.whisperer_for_laravel.actions.provider;

import at.alirezamoh.whisperer_for_laravel.support.WhispererForLaravelIcon;
import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class ChooseActionModel implements ChooseByNameModel {
    private AnAction[] allActions;

    public ChooseActionModel() {
        DefaultActionGroup actionGroup = (DefaultActionGroup) ActionManager.getInstance()
            .getAction("at.alirezamoh.idea_whisperer_for_laravel.AllCodeGenerationActionsGroupAction");

        this.allActions = actionGroup.getChildActionsOrStubs();
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) String getPromptText() {
        return "Enter action name:";
    }

    @Override
    public @NotNull @NlsContexts.Label String getNotInMessage() {
        return "No actions found";
    }

    @Override
    public @NotNull @NlsContexts.Label String getNotFoundMessage() {
        return "No actions found";
    }

    @Override
    public @Nullable @NlsContexts.Label String getCheckBoxName() {
        return "";
    }

    @Override
    public boolean loadInitialCheckBoxState() {
        return false;
    }

    @Override
    public void saveInitialCheckBoxState(boolean state) {

    }

    @Override
    public @NotNull ListCellRenderer getListCellRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
            {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (component instanceof JLabel jLabel && value instanceof AnAction) {
                    jLabel.setIcon(WhispererForLaravelIcon.LARAVEL_ICON);
                    jLabel.setText("<html><b>" + ((AnAction) value).getTemplatePresentation().getText() + "</b></html>");
                    jLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            }
                return component;
            }
        };
    }

    @Override
    public String @NotNull @Nls [] getNames(boolean checkBoxState) {
        return Arrays.stream(allActions)
            .map(action -> action.getTemplatePresentation().getText())
            .toArray(String[]::new);
    }

    @Override
    public Object @NotNull [] getElementsByName(@NotNull String name, boolean checkBoxState, @NotNull String pattern) {
        return Arrays.stream(allActions)
            .filter(action -> action.getTemplatePresentation().getText().equals(name))
            .toArray();
    }

    @Override
    public @Nullable String getElementName(@NotNull Object element) {
        if (element instanceof AnAction action) {
            return action.getTemplatePresentation().getText();
        }
        return null;
    }

    @Override
    public String @NotNull [] getSeparators() {
        return new String[0];
    }

    @Override
    public @Nullable String getFullName(@NotNull Object element) {
        return getElementName(element);
    }

    @Override
    public @Nullable @NonNls String getHelpId() {
        return null;
    }

    @Override
    public boolean willOpenEditor() {
        return false;
    }

    @Override
    public boolean useMiddleMatching() {
        return true;
    }
}

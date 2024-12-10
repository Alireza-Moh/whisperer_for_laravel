package at.alirezamoh.idea_whisperer_for_laravel.actions;

import at.alirezamoh.idea_whisperer_for_laravel.actions.provider.ChooseActionModel;
import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.ide.util.gotoByName.ChooseByNamePopupComponent;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class ActionChooserAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        ChooseByNameModel model = new ChooseActionModel();
        ChooseByNamePopup popup = ChooseByNamePopup.createPopup(project, model, this.getPsiContext(e));
        popup.setShowListForEmptyPattern(false);
        popup.invoke(new ChooseByNamePopupComponent.Callback() {
            @Override
            public void elementChosen(Object o) {
                if (o instanceof AnAction action) {
                    ActionManager actionManager = ActionManager.getInstance();
                    actionManager.tryToExecute(
                            action,
                            e.getInputEvent(),
                            e.getDataContext().getData(PlatformDataKeys.CONTEXT_COMPONENT),
                            e.getPlace(),
                            true
                    );
                }
            }
        }, ModalityState.current(), false);
    }

    private PsiElement getPsiContext(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        return dataContext.getData(CommonDataKeys.PSI_ELEMENT);
    }
}

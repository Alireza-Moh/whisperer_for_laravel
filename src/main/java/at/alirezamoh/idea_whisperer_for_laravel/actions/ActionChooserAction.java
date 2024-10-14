package at.alirezamoh.idea_whisperer_for_laravel.actions;

import at.alirezamoh.idea_whisperer_for_laravel.actions.provider.ChooseActionModel;
import at.alirezamoh.idea_whisperer_for_laravel.support.notification.Notify;
import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.ide.util.gotoByName.ChooseByNamePopupComponent;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
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
                if (o instanceof AnAction) {
                    try {
                        AnAction action = (AnAction) o;
                        action.actionPerformed(e);
                    } catch (Exception e) {
                        Notify.notifyError(
                            project,
                            "Could not execute action "
                        );
                    }
                }
            }
        }, ModalityState.current(), false);
    }

    private PsiElement getPsiContext(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        return dataContext.getData(CommonDataKeys.PSI_ELEMENT);
    }
}

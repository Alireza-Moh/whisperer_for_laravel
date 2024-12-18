package at.alirezamoh.whisperer_for_laravel.actions;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class EloquentNonGroupAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        AnAction secondAction = ActionManager.getInstance().getAction("create.EloquentAction");

        if (secondAction != null) {
            secondAction.actionPerformed(anActionEvent);
        }
    }
}

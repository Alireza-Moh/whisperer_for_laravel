package at.alirezamoh.whisperer_for_laravel.actions;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import org.jetbrains.annotations.NotNull;

public class EloquentNonGroupAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        ActionManager actionManager = ActionManager.getInstance();
        AnAction modelAction = actionManager.getAction("at.alirezamoh.whisperer_for_laravel.EloquentAction");

        if (modelAction != null) {
            actionManager.tryToExecute(
                modelAction,
                anActionEvent.getInputEvent(),
                anActionEvent.getDataContext().getData(PlatformDataKeys.CONTEXT_COMPONENT),
                anActionEvent.getPlace(),
                true
            );
        }
    }
}

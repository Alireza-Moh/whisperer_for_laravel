package at.alirezamoh.idea_whisperer_for_laravel.actions;

import at.alirezamoh.idea_whisperer_for_laravel.support.ComposerUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.MethodUtils;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class StartPluginAction extends DefaultActionGroup {

    StartPluginAction() {
        super("Idea Whisperer For Laravel", true);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null && !MethodUtils.isDumbMode(project)) {
            if (!ComposerUtils.isLaravelProject(project)) {
                e.getPresentation().setEnabledAndVisible(false);
            }
        }

        if (project == null) {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}

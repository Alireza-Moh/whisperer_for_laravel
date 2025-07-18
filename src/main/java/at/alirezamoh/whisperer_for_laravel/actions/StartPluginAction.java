package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class StartPluginAction extends DefaultActionGroup {

    StartPluginAction() {
        super("Whisperer For Laravel", true);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        boolean isVisible =
            project != null
            && PluginUtils.isNotInDumbMode(project)
            && PluginUtils.isLaravelProject(project);

        e.getPresentation().setEnabledAndVisible(isVisible);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}

package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.support.codeGeneration.HelperCodeGenerationTask;
import at.alirezamoh.whisperer_for_laravel.support.notification.Notify;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class GenerateHelperMethodsAction extends BaseAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();

        if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
            Notify.notifyWarning(project, "Laravel Framework is not installed");
            return;
        }

        new HelperCodeGenerationTask(project).queue();
    }
}

package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.views.ConfigFileView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ConfigAction extends BaseAction {
    /**
     * Invoked when the action is performed
     * @param anActionEvent The action event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        ConfigFileView configFileView = new ConfigFileView(project);

        if (configFileView.showAndGet()) {
            this.create(configFileView.getConfigFileModel(), "configFile.ftl", true, project);
        }
    }
}

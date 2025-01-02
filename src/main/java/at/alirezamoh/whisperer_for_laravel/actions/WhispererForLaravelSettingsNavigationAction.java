package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.settings.SettingConfigurable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class WhispererForLaravelSettingsNavigationAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();

        if (project == null) return;

        ShowSettingsUtil.getInstance().showSettingsDialog(anActionEvent.getProject(), SettingConfigurable.class);
    }
}

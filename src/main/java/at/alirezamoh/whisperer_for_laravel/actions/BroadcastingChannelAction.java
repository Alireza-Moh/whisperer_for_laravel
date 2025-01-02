package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.views.BroadcastingChannelView;
import at.alirezamoh.whisperer_for_laravel.actions.views.ControllerView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class BroadcastingChannelAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        BroadcastingChannelView broadcastingChannelView = new BroadcastingChannelView(project);

        if (broadcastingChannelView.showAndGet()) {
            this.create(
                broadcastingChannelView.getBroadcastingChannelModel(),
                "broadcastingChannel.ftl",
                true,
                project
            );
        }
    }
}

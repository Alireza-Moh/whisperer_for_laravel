package at.alirezamoh.idea_whisperer_for_laravel.actions;

import at.alirezamoh.idea_whisperer_for_laravel.actions.views.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ConsoleAction extends BaseAction {
    /**
     * Invoked when the action is performed
     * @param anActionEvent The action event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();

        ConsoleView consoleView = new ConsoleView(project);

        if (consoleView.showAndGet()) {
            this.create(
                consoleView.getConsoleModel(),
                "console.ftl",
                true,
                project
            );
        }
    }
}

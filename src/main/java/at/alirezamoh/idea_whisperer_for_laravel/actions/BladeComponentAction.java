package at.alirezamoh.idea_whisperer_for_laravel.actions;

import at.alirezamoh.idea_whisperer_for_laravel.actions.views.BladeComponentView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class BladeComponentAction extends BaseAction {
    /**
     * Invoked when the action is performed
     * @param anActionEvent The action event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();

        BladeComponentView bladeComponentView = new BladeComponentView(project);

        if (bladeComponentView.showAndGet()) {

            if (bladeComponentView.withBladeComponentClassAndBladeView()) {
                this.create(
                    bladeComponentView.getBladeComponentViewModel(),
                    "view.ftl",
                    true,
                    project
                );
                this.create(
                    bladeComponentView.getBladeComponentClassModel(),
                    "bladeComponentClass.ftl",
                    true,
                    project
                );
            }

            if (bladeComponentView.onlyComponentBladeView()) {
                this.create(
                    bladeComponentView.getBladeComponentViewModel(),
                    "view.ftl",
                    true,
                    project
                );
            }

            if (bladeComponentView.onlyComponentClass()) {
                this.create(
                    bladeComponentView.getBladeComponentClassModel(),
                    "bladeComponentClass.ftl",
                    true,
                    project
                );
            }
        }
    }
}

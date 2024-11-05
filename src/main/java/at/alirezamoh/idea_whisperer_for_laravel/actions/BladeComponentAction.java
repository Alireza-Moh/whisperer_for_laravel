package at.alirezamoh.idea_whisperer_for_laravel.actions;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.BladeComponentClassModel;
import at.alirezamoh.idea_whisperer_for_laravel.actions.models.BladeComponentViewModel;
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

            BladeComponentClassModel componentClass = bladeComponentView.getBladeComponentClassModel();
            BladeComponentViewModel viewForComponent = bladeComponentView.getBladeComponentViewModel();

            if (bladeComponentView.withBladeComponentClassAndBladeView()) {
                createView(viewForComponent, project);
                createComponentClass(componentClass, project);
            }

            if (bladeComponentView.onlyComponentBladeView()) {
                createView(viewForComponent, project);
            }

            if (bladeComponentView.onlyComponentClass()) {
                createComponentClass(componentClass, project);
            }
        }
    }

    private void createView(BladeComponentViewModel model, Project project) {
        this.create(
            model,
            "view.ftl",
            true,
            project
        );
    }

    private void createComponentClass(BladeComponentClassModel model, Project project) {
        this.create(
            model,
            "bladeComponentClass.ftl",
            true,
            project
        );
    }
}

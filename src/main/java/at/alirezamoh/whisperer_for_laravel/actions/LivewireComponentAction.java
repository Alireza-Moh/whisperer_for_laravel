package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.models.LivewireComponentClassModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.LivewireComponentViewModel;
import at.alirezamoh.whisperer_for_laravel.actions.views.LivewireComponentView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class LivewireComponentAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();

        LivewireComponentView livewireComponentView = new LivewireComponentView(project);

        if (livewireComponentView.showAndGet()) {

            LivewireComponentClassModel componentClass = livewireComponentView.getLivewireComponentClassModel();
            LivewireComponentViewModel viewForComponent = livewireComponentView.getLivewireComponentViewModel(componentClass);

            componentClass.setViewName(viewForComponent.viewNameForParent);

            if (!componentClass.isInline()) {
                this.create(
                    viewForComponent,
                    "view.ftl",
                    true,
                    project
                );
            }

            this.create(
                componentClass,
                "livewireComponentClass.ftl",
                true,
                project
            );
        }
    }
}

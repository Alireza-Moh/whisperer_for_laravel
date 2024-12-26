package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.views.JsonResourceCollectionView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class JsonResourceCollectionAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        JsonResourceCollectionView jsonResourceCollectionView = new JsonResourceCollectionView(project);

        if (jsonResourceCollectionView.showAndGet()) {
            this.create(
                jsonResourceCollectionView.getJsonResourceCollectionModel(),
                "jsonResourceCollection.ftl",
                true,
                project
            );
        }
    }
}

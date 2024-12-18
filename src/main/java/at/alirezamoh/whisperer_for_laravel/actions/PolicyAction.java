package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.views.PolicyView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class PolicyAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        PolicyView policyView = new PolicyView(project);

        if (policyView.showAndGet()) {
            this.create(
                policyView.getPolicyModel(),
                "policy.ftl",
                true,
                project
            );
        }
    }
}

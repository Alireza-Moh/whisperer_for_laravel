package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.views.ValidationRuleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ValidationRuleAction extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        ValidationRuleView validationRuleView = new ValidationRuleView(project);

        if (validationRuleView.showAndGet()) {
            this.create(
                validationRuleView.getValidationRuleModel(),
                "validationRule.ftl",
                true,
                project
            );
        }
    }
}

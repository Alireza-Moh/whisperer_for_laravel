package at.alirezamoh.whisperer_for_laravel.actions;

import at.alirezamoh.whisperer_for_laravel.actions.models.BaseModel;
import at.alirezamoh.whisperer_for_laravel.support.template.TemplateLoader;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class BaseAction extends AnAction {
    /**
     * Creates a file from a template
     * @param model         The data model for the template.
     * @param templateName  The name of the template file.
     * @param openInEditor  Whether to open the created file in the editor.
     * @param project       The current project.
     */
    protected void create(BaseModel model, String templateName, boolean openInEditor, Project project) {
        TemplateLoader templateProcessor = new TemplateLoader(
            project,
            templateName,
            model
        );

        templateProcessor.createTemplateWithDirectory(openInEditor);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}

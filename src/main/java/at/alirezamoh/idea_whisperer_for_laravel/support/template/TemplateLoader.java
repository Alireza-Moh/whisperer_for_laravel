package at.alirezamoh.idea_whisperer_for_laravel.support.template;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.BaseModel;
import at.alirezamoh.idea_whisperer_for_laravel.support.notification.Notify;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Loads and processes Freemarker templates
 * It creates php files
 */
public class TemplateLoader {
    /**
     * The current project
     */
    private Project project;

    /**
     * The Freemarker configuration
     */
    private Configuration configuration;

    /**
     * The name of the freemaker template file
     */
    private String template;

    /**
     * The data model for the template
     */
    private BaseModel object;

    private boolean showSuccessMessage = true;

    private boolean overwriteFile = false;

    /**
     * @param project  The current project
     * @param template The name of the template file
     * @param object   The data model for the template
     */
    public TemplateLoader(Project project, String template, BaseModel object) {
        this.project = project;
        this.template = template;
        this.object = object;
    }

    /**
     * @param project                     The current project
     * @param template                    The name of the template file
     * @param object                      The data model for the template
     * @param showSuccessOrErrorMessage   show message to user
     * @param overwriteFile               should overwrite file
     */
    public TemplateLoader(Project project, String template, BaseModel object, boolean showSuccessOrErrorMessage, boolean overwriteFile) {
        this.project = project;
        this.template = template;
        this.object = object;
        this.showSuccessMessage = showSuccessOrErrorMessage;
        this.overwriteFile = overwriteFile;
    }

    /**
     * Creates the template file and its directory if it doesn't exist.
     * Opens the file in the IDE if needed
     *
     * @param openInEditor Whether to open the created file in the editor
     */
    public void createTemplateWithDirectory(boolean openInEditor) {
        ApplicationManager.getApplication().invokeLater(() -> {

            WriteCommandAction.runWriteCommandAction(project, () -> {
                try {
                    VirtualFile virtualFile = VfsUtil.createDirectories(getDirectoryWithoutSlash());
                    PsiFile createdFile = createTemplateOnly();
                    virtualFile.refresh(false, false);

                    if (openInEditor && createdFile != null) {
                        openFileInEditor(createdFile);
                    }
                } catch (IOException e) {
                    Notify.notifyError(
                        project,
                        "Could not create " + object.getName() + " directory"
                    );
                }
            });
        });

    }

    /**
     * Creates the php file from the template
     */
    public @Nullable PsiFile createTemplateOnly() {
        PsiFile createdFile = null;
        try {
            String filePath = project.getBasePath() + "/" + object.getFilePath();
            File file = new File(filePath);
            if (file.exists() && !overwriteFile) {
                Notify.notifyWarning(
                    project,
                    object.getName() + " file already exists"
                );
                return null;
            }

            Writer writer = new FileWriter(filePath);

            configuration = new Configuration(Configuration.VERSION_2_3_32);
            configuration.setTemplateLoader(
                new ClassTemplateLoader(getClass().getClassLoader(), "/templates")
            );
            configuration.setDefaultEncoding("UTF-8");
            configuration.getTemplate(template).process(object, writer);

            writer.close();

            VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(filePath);
            if (virtualFile != null) {
                createdFile = PsiManager.getInstance(project).findFile(virtualFile);
            }

            if (showSuccessMessage) {
                Notify.notifySuccess(
                    project,
                    object.getName() + " created successfully"
                );
            }
        } catch (IOException | TemplateException ex) {
            Notify.notifyError(
                project,
                "Could not create " + object.getName() + " file"
            );
        }

        return createdFile;
    }

    /**
     * Returns the directory path without a trailing slash
     * @return The directory path
     */
    private String getDirectoryWithoutSlash() {
        String dirName;
        if (object.getDestination().startsWith("/")) {
            dirName = project.getBasePath() + object.getDestination();
        }
        else {
            dirName = project.getBasePath() + "/" + object.getDestination();
        }

        if (dirName.endsWith("/")) {
            dirName = dirName.substring(0, dirName.length() - 1);
        }

        return dirName;
    }

    /**
     * Opens the created file in the editor
     * @param createdFile The created php file
     */
    public void openFileInEditor(PsiFile createdFile) {
        ReadAction.run(() -> {
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, createdFile.getVirtualFile());
            FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
        });
    }
}

package at.alirezamoh.whisperer_for_laravel.support.codeGeneration;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A background task that generates helper code
 * <p>
 * This task runs in the background with a progress indicator, allowing
 * the IDE to display progress and support task cancellation.
 * </p>
 * <p>
 * The generation logic is delegated to {@link HelperCodeExecutor}.
 * </p>
 */
public class HelperCodeGenerationTask extends Task.Backgroundable {
    public HelperCodeGenerationTask(@Nullable Project project) {
        super(project, "Generating helper code", false);
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        HelperCodeExecutor helperCodeExecutor = new HelperCodeExecutor(
            myProject,
            progressIndicator,
            true
        );

        helperCodeExecutor.execute();
    }
}

package at.alirezamoh.whisperer_for_laravel.support;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.utils.DirectoryUtils;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class ProjectLocaleLangResolver {
    private static final List<String> ENV_FILES = Arrays.asList("/.env", "/.env.example", "/.env.local", "/.env.testing", "/.env.production");

    public static @Nullable String getAppLocale(Project project) {
        return ReadAction.compute(() -> {
            PsiFile envFile = findFirstExistingEnvFile(project);
            if (envFile == null) {
                return null;
            }

            String[] lines = envFile.getText().split("\\r?\\n");

            for (String line : lines) {
                String trimmedLine = line.trim();

                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    continue;
                }

                if (trimmedLine.startsWith("APP_LOCALE")) {
                    String[] parts = trimmedLine.split("=", 2);
                    if (parts.length == 2) {
                        return parts[1].trim();
                    }
                }
            }

            return null;
        });
    }

    public static @Nullable String loadProjectLocale(Project project) {
        SettingsState settingsState = SettingsState.getInstance(project);
        return settingsState.getDefaultProjectLang();
    }

    private static @Nullable PsiFile findFirstExistingEnvFile(Project project) {
        for (String filename : ENV_FILES) {
            PsiFile file = DirectoryUtils.getFileByName(project, filename);
            if (file != null) {
                return file;
            }
        }
        return null;
    }
}

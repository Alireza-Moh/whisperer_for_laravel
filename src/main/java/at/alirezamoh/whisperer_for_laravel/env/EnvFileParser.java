package at.alirezamoh.whisperer_for_laravel.env;

import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class EnvFileParser {
    public static Map<String, String> parseEnvFile(Project project) {
        Map<String, String> envMap = new HashMap<>();

        File envFile = getEnvFile(project);
        if (!envFile.exists()) {
            return envMap;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int equalSignIndex = line.indexOf('=');
                if (equalSignIndex == -1) {
                    continue;
                }

                String key = line.substring(0, equalSignIndex).trim();
                String value = getValue(line, equalSignIndex);

                envMap.put(key, value);
            }
        } catch (IOException e) {
            return envMap;
        }

        return envMap;
    }

    private static @NotNull String getValue(String line, int equalSignIndex) {
        String value = line.substring(equalSignIndex + 1).trim();

        if ((value.startsWith("\"") && value.endsWith("\"")) ||
            (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static @NotNull File getEnvFile(Project project) {
        String filePath = project.getBasePath() + "/.env";
        SettingsState settings = SettingsState.getInstance(project);
        if (!settings.isLaravelDirectoryEmpty()) {
            filePath = project.getBasePath() + StrUtil.addSlashes(settings.getLaravelDirectoryPath()) + ".env";
        }

        return new File(StrUtil.removeDoubleSlashes(filePath));
    }
}

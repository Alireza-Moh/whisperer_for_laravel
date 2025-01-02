package at.alirezamoh.whisperer_for_laravel.config.util;

import at.alirezamoh.whisperer_for_laravel.config.visitors.ArrayReturnPsiRecursiveVisitor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import java.util.Map;
import java.util.regex.Pattern;

public class ConfigUtil {
    public static void iterateOverFileChildren(String dirName, PsiFile configFile, Map<String, String> variants) {
        ArrayReturnPsiRecursiveVisitor visitor = new ArrayReturnPsiRecursiveVisitor(dirName);
        configFile.acceptChildren(visitor);

        variants.put(dirName, null);
        variants.putAll(visitor.getVariants());
    }

    public static String buildParentPathForConfigKey(VirtualFile file, Project project, boolean forModule, String configKeyIdentifier) {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return "";
        }

        String fullPath = file.getPath();

        fullPath = fullPath.replaceFirst("(?i)^" + Pattern.quote(basePath), "");

        while (fullPath.startsWith("/")) {
            fullPath = fullPath.substring(1);
        }

        if (fullPath.startsWith("config/")) {
            fullPath = fullPath.substring("config/".length());
        }

        if (fullPath.endsWith(".php")) {
            fullPath = fullPath.substring(0, fullPath.length() - 4);
        }

        fullPath = fullPath.replace('/', '.');

        if (forModule) {
            fullPath = configKeyIdentifier;
        }

        return fullPath;
    }
}

package at.alirezamoh.idea_whisperer_for_laravel.gate.util;

import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.ClassUtils;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.MethodReference;

import java.util.Objects;

public class GateUtil {
    public static boolean isGateFacadeMethod(MethodReference method, Project project) {
        return ClassUtils.isLaravelRelatedClass(method, project)
            && Objects.equals(method.getName(), "define");
    }
}

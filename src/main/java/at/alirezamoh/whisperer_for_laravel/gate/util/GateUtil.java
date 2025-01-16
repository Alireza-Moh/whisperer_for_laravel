package at.alirezamoh.whisperer_for_laravel.gate.util;

import at.alirezamoh.whisperer_for_laravel.gate.GateReferenceContributor;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.MethodReference;

import java.util.Objects;

public class GateUtil {
    public static boolean isGateFacadeMethod(MethodReference method, Project project) {
        return PhpClassUtils.isCorrectRelatedClass(method, project, GateReferenceContributor.GATE)
            && Objects.equals(method.getName(), "define");
    }
}

package at.alirezamoh.whisperer_for_laravel.gate.util;

import at.alirezamoh.whisperer_for_laravel.gate.GateReferenceContributor;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.MethodReference;

import java.util.Objects;

public class GateUtil {
    /**
     * Determines if the given method reference corresponds to
     * a Gate facade call to the <code>define</code> method
     *
     * @param method  The method reference to check
     * @param project The current project
     * @return true or false
     */
    public static boolean isGateFacadeMethod(MethodReference method, Project project) {
        return PhpClassUtils.isCorrectRelatedClass(method, project, GateReferenceContributor.GATE)
            && Objects.equals(method.getName(), "define");
    }
}

package at.alirezamoh.whisperer_for_laravel.facade.util;

import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;

public class RealTimeFacadeUtil {
    /**
     * Retrieves the PhpClass for a given facade fully qualified name
     * It removes the "\Facades" part from the FQN before performing the lookup
     *
     * @param project   the current project context
     * @param facadeFqn the fully qualified name of the facade
     * @return the corresponding PhpClass, or null if not found
     */
    public static @Nullable PhpClass getFacadeClass(Project project, String facadeFqn) {
        if (facadeFqn == null) {
            return null;
        }

        if (!facadeFqn.startsWith("\\Facades\\")) {
            return null;
        }

        return PhpClassUtils.getClassByFQN(project, facadeFqn.replace("\\Facades", ""));
    }
}

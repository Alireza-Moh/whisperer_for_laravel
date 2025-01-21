package at.alirezamoh.whisperer_for_laravel.support.utils;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.Collection;

public class PhpIndexUtils {
    /**
     * Retrieves all {@link PhpClass} with the specified name from the {@link PhpIndex}
     *
     * @param name    The PHP class name to search for
     * @param project The current project
     * @return A collection of matching {@link PhpClass} instances
     */
    public static Collection<PhpClass> getPhpClassesByName(String name, Project project) {
        return PhpIndex.getInstance(project).getClassesByName(name);
    }

    /**
     * Retrieves a single {@link PhpClass} matching the specified name from the {@link PhpIndex}.
     *
     * @param name    The PHP class name to search for
     * @param project The current project
     * @return A matching {@link PhpClass}, or {@code null} if none exists
     */
    public static PhpClass getPhpClassByName(String name, Project project) {
        return PhpIndex.getInstance(project).getClassByName(name);
    }
}

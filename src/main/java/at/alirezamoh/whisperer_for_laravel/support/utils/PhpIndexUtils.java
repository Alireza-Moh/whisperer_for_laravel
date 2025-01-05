package at.alirezamoh.whisperer_for_laravel.support.utils;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.Collection;

public class PhpIndexUtils {
    public static Collection<PhpClass> getPhpClassesByName(String name, Project project) {
        return PhpIndex.getInstance(project).getClassesByName(name);
    }

    public static PhpClass getPhpClassByName(String name, Project project) {
        return PhpIndex.getInstance(project).getClassByName(name);
    }
}

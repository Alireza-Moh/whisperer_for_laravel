package at.alirezamoh.whisperer_for_laravel.support.laravelUtils;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassAliasImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClassUtils {
    public static @Nullable PhpClass getEloquentBaseModel(Project project) {
        return PhpIndex.getInstance(project)
            .getClassesByFQN(LaravelPaths.LaravelClasses.Model)
            .stream()
            .findFirst()
            .orElse(null);
    }

    public static @Nullable PhpClass getClassByFQN(Project project, String path) {
        return PhpIndex.getInstance(project)
            .getClassesByFQN(path)
            .stream()
            .findFirst()
            .orElse(null);
    }

    public static boolean isChildOf(PhpClassImpl phpClass, PhpClass clazz) {
        if (phpClass.getFQN().equals(clazz.getFQN())) {
            return true;
        }

        PhpClass superClass = phpClass.getSuperClass();
        if (superClass == null) {
            return false;
        }

        if (superClass instanceof PhpClassAliasImpl aliasClass) {
            PhpClass original = aliasClass.getOriginal();
            if (original == null) {
                return false;
            }
            return isChildOf((PhpClassImpl) original, clazz);
        }

        return superClass instanceof PhpClassImpl && isChildOf((PhpClassImpl) superClass, clazz);
    }

    public static boolean isLaravelRelatedClass(MethodReference methodReference, Project project) {
        List<PhpClassImpl> resolvedClasses = MethodUtils.resolveMethodClasses(methodReference, project);

        for (PhpClassImpl clazz : resolvedClasses) {
            for (String classFQN : LaravelPaths.RELEVANT_LARAVEL_CLASSES) {
                PhpClass foundedClass = getClassByFQN(project, classFQN);

                if (foundedClass != null && isChildOf(clazz, foundedClass)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isCorrectRelatedClass(MethodReference methodReference, Project project, List<String> classFQNs) {
        List<PhpClassImpl> resolvedClasses = MethodUtils.resolveMethodClasses(methodReference, project);

        for (PhpClassImpl clazz : resolvedClasses) {
            for (String classFQN : classFQNs) {
                PhpClass foundedClass = getClassByFQN(project, classFQN);

                if (foundedClass != null && isChildOf(clazz, foundedClass)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isCorrectRelatedClass(MethodReference methodReference, Project project, String classFqn) {
        List<PhpClassImpl> resolvedClasses = MethodUtils.resolveMethodClasses(methodReference, project);

        for (PhpClassImpl clazz : resolvedClasses) {
            PhpClass foundedClass = getClassByFQN(project, classFqn);

            if (foundedClass != null && isChildOf(clazz, foundedClass)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Resolves a PHP class from a typed element
     *
     * @param typedElement the PHP typed element (e.g., variable or reference)
     * @param project the current project
     * @return the resolved PhpClassImpl instance or null if not found
     */
    public static PhpClassImpl getClassFromTypedElement(PhpTypedElement typedElement, Project project) {
        if (typedElement == null) {
            return null;
        }

        PhpIndex phpIndex = PhpIndex.getInstance(project);
        String classFQN = typedElement.getDeclaredType().getTypes().stream().findFirst().orElse("");

        return phpIndex.getClassesByFQN(classFQN).stream()
            .filter(clazz -> clazz instanceof PhpClassImpl)
            .map(clazz -> (PhpClassImpl) clazz)
            .findFirst()
            .orElse(null);
    }
}

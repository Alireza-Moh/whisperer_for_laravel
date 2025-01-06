package at.alirezamoh.whisperer_for_laravel.support.utils;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassAliasImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PhpClassUtils {
    /**
     * Retrieves all public methods of a given PHP class excluding constructor
     *
     * @param phpClass The PhpClass instance to extract public methods from.
     * @return A list of public methods (non-magic) defined in the given class
     */
    public static List<Method> getClassPublicMethod(PhpClass phpClass) {
        ArrayList<Method> methods = new ArrayList<>();

        for(Method method: phpClass.getMethods()) {
            if(method.getAccess().isPublic() && !method.getName().startsWith("__")) {
                methods.add(method);
            }
        }

        return methods;
    }

    /**
     * Retrieves the base Eloquent model class (`Illuminate\Database\Eloquent\Model`) from the project
     *
     * @param project the current PhpStorm project instance
     * @return the PhpClass representing the base Eloquent model, or null if not found
     */
    public static @Nullable PhpClass getEloquentBaseModel(Project project) {
        return PhpIndex.getInstance(project)
            .getClassesByFQN(LaravelPaths.LaravelClasses.Model)
            .stream()
            .findFirst()
            .orElse(null);
    }

    /**
     * Retrieves a PHP class by its fully qualified name (FQN)
     *
     * @param project the current PhpStorm project instance
     * @param path the fully qualified name (FQN) of the class to search for
     * @return the PhpClass representing the specified class, or null if not found
     */
    public static @Nullable PhpClass getClassByFQN(Project project, String path) {
        return PhpIndex.getInstance(project)
            .getClassesByFQN(path)
            .stream()
            .findFirst()
            .orElse(null);
    }

    /**
     * Checks if a PHP class is a child (or descendant) of another PHP class
     *
     * @param phpClass the PhpClass to check
     * @param clazz the potential parent PhpClass
     * @return true or false
     */
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

    /**
     * Checks if the provided method reference is related to any of the specified classes
     * (or their descendants) in the given project.
     *
     * @param methodReference the method reference to check
     * @param project the current PhpStorm project instance
     * @param classFQNs one or more fully qualified class names (FQNs) to check against
     * @return true if the method reference is related to any of the specified classes, false otherwise
     */
    public static boolean isCorrectRelatedClass(MethodReference methodReference, Project project, String... classFQNs) {
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

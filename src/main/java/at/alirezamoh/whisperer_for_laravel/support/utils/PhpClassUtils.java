package at.alirezamoh.whisperer_for_laravel.support.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassAliasImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PhpClassUtils {
    /**
     * Retrieves all public methods of a given PHP class excluding constructor
     *
     * @param phpClass The PhpClass instance to extract public methods from.
     * @return A list of public methods (non-magic) defined in the given class
     */
    public static List<Method> getClassPublicMethods(PhpClass phpClass, boolean ownMethods) {
        ArrayList<Method> methods = new ArrayList<>();

        Method[] foundedMethods = ownMethods ? phpClass.getOwnMethods() : phpClass.getMethods().toArray(new Method[0]);

        for(Method method: foundedMethods) {
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
     * @param phpClassToCheck the PhpClass to check
     * @param parentPhpClass the parent PhpClass
     * @return true or false
     */
    public static boolean isChildOf(PhpClassImpl phpClassToCheck, PhpClass parentPhpClass) {
        if (phpClassToCheck.getFQN().equals(parentPhpClass.getFQN())) {
            return true;
        }

        PhpClass superClass = phpClassToCheck.getSuperClass();
        if (superClass == null) {
            return false;
        }

        if (superClass instanceof PhpClassAliasImpl aliasClass) {
            PhpClass original = aliasClass.getOriginal();
            if (original == null) {
                return false;
            }
            return isChildOf((PhpClassImpl) original, parentPhpClass);
        }

        return superClass instanceof PhpClassImpl && isChildOf((PhpClassImpl) superClass, parentPhpClass);
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

    public static @Nullable PhpClass getPhpClassFromFile(PhpFile phpFile, String className) {
        for (PhpNamedElement topLevelElement : phpFile.getTopLevelDefs().values()) {
            if (topLevelElement instanceof PhpClass clazz && clazz.getName().equals(className)) {
                return clazz;
            }
        }

        return null;
    }

    public static Collection<PhpClass> getPhpClassesFromFile(PhpFile phpFile) {
        Collection<PhpClass> classes = new ArrayList<>();

        for (PhpNamedElement topLevelElement : phpFile.getTopLevelDefs().values()) {
            if (topLevelElement instanceof PhpClass clazz) {
                classes.add(clazz);
            }
        }

        return classes;
    }

    public static @Nullable PhpClass getPhpClassFromMethodRef(MethodReference methodReference) {
        if (methodReference == null) {
            return null;
        }

        ClassReferenceImpl classReference = getClassReferenceImplFromMethodRef(methodReference);
        if (classReference == null) {
            return null;
        }

        PsiReference reference = classReference.getReference();
        if (reference == null) {
            return null;
        }

        PsiElement resolved = reference.resolve();
        if (!(resolved instanceof PhpClass possiblePhpClass)) {
            return null;
        }

        return possiblePhpClass;
    }

    public static @Nullable ClassReferenceImpl getClassReferenceImplFromMethodRef(MethodReference methodReference) {
        if (methodReference == null) {
            return null;
        }

        PhpExpression phpExpression = methodReference.getClassReference();
        if (!(phpExpression instanceof ClassReferenceImpl classReference)) {
            return null;
        }

        return classReference;
    }

}

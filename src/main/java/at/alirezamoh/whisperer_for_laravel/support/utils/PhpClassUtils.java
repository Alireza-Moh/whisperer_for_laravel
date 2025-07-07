package at.alirezamoh.whisperer_for_laravel.support.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassAliasImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PhpClassUtils {
    private static final Logger log = LoggerFactory.getLogger(PhpClassUtils.class);

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

    /**
     * Retrieves a PhpClass from a PhpFile by its class name
     *
     * @param phpFile the PhpFile to search in
     * @param className the name of the class to find
     * @return the PhpClass if found, or null if not found
     */
    public static @Nullable PhpClass getPhpClassFromFile(PhpFile phpFile, String className) {
        for (PhpNamedElement topLevelElement : phpFile.getTopLevelDefs().values()) {
            if (topLevelElement instanceof PhpClass clazz && clazz.getName().equals(className)) {
                return clazz;
            }
        }

        return null;
    }

    /**
     * Retrieves all PhpClasses defined in a given PhpFile
     *
     * @param phpFile the PhpFile to search in
     * @return a collection of PhpClass instances found in the file
     */
    public static Collection<PhpClass> getPhpClassesFromFile(PhpFile phpFile) {
        Collection<PhpClass> classes = new ArrayList<>();

        for (PhpNamedElement topLevelElement : phpFile.getTopLevelDefs().values()) {
            if (topLevelElement instanceof PhpClass clazz) {
                classes.add(clazz);
            }
        }

        return classes;
    }

    /**
     * Retrieves the PhpClass from a MethodReference element.
     * If the element is not a MethodReference or cannot be resolved, returns null.
     *
     * @param methodReference the PSI element to check
     * @return the resolved PhpClass or null if not found
     */
    public static @Nullable PhpClass getCachedPhpClassFromMethodRef(MethodReference methodReference) {
        if (methodReference == null) {
            return null;
        }

        return CachedValuesManager.getCachedValue(methodReference, () ->
            CachedValueProvider.Result.create(resolvePhpClassFromMethodRef(methodReference), methodReference)
        );
    }

    /**
     * Retrieves the ClassReferenceImpl from a MethodReference element.
     * If the element is not a MethodReference or does not have a class reference, returns null.
     *
     * @param methodReference the PSI element to check
     * @return the ClassReferenceImpl or null if not found
     */
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

    /**
     * Checks if the provided PhpClass is a factory class
     * by checking if it extends the base factory class
     *
     * @param phpClass The PhpClass to check
     * @return true or false
     */
    public static boolean isChildOfBaseClass(PhpClass phpClass, Project project, String ...baseClassNamespace) {
        if (!(phpClass instanceof PhpClassImpl)) {
            return false;
        }

        for (String classFQN : baseClassNamespace) {
            PhpClass baseClass = PhpClassUtils.getClassByFQN(project, classFQN);

            if (baseClass != null && PhpClassUtils.isChildOf((PhpClassImpl) phpClass, baseClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the PhpClass from a ClassConstantReference element.
     * If the element is not a ClassConstantReference or cannot be resolved, returns null.
     *
     * @param classConstant the PSI element to check
     * @return the resolved PhpClass or null if not found
     */
    public static @Nullable PhpClass getCachedPhpClassFromClassConstant(@Nullable PsiElement classConstant) {
        if (classConstant == null) {
            return null;
        }

        return CachedValuesManager.getCachedValue(classConstant, () ->
            CachedValueProvider.Result.create(resolvePhpClass(classConstant), classConstant)
        );
    }

    /**
     * Retrieves the file where a ClassConstantReference is defined.
     * If the element is not a ClassConstantReference or cannot be resolved, returns null.
     *
     * @param element the PSI element to check
     * @return the containing PsiFile or null if not found
     */
    public static @Nullable PsiFile getContainingFileFromClassConstant(@Nullable PsiElement element) {
        PhpClass resolvedPhpClass = getCachedPhpClassFromClassConstant(element);
        if (resolvedPhpClass == null) {
            return null;
        }

        return resolvedPhpClass.getContainingFile();
    }

    /**
     * Retrieves the containing PhpClass from a MethodReference element.
     * If the element is not a MethodReference or cannot be resolved, returns null.
     *
     * @param methodReference the PSI element to check
     * @return the containing PhpClass or null if not found
     */
    public static @Nullable PhpClass getCachedContainingPhpClassFromMethodRef(MethodReference methodReference) {
        if (methodReference == null) {
            return null;
        }

        return CachedValuesManager.getCachedValue(methodReference, () ->
            CachedValueProvider.Result.create(
                doGetContainingPhpClassFromMethodRef(methodReference),
                methodReference
            )
        );

    }

    /**
     * Resolves the PhpClass from a ClassConstantReference element.
     * If the element is not a ClassConstantReference or cannot be resolved, returns null.
     *
     * @param classConstant the PSI element to check
     * @return the resolved PhpClass or null if not found
     */
    private static @Nullable PhpClass resolvePhpClass(@NotNull PsiElement classConstant) {
        if (!(classConstant instanceof ClassConstantReference classConstantReference)) {
            return null;
        }

        PhpExpression reference = classConstantReference.getClassReference();
        if (!(reference instanceof ClassReference classReference)) {
            return null;
        }

        return getPhpClassFromPsiReference(classReference);
    }

    /**
     * Retrieves the PhpClass from a ClassReference element.
     * If the element is not a ClassReference or cannot be resolved, returns null.
     *
     * @param classReference the PSI element to check
     * @return the resolved PhpClass or null if not found
     */
    private static @Nullable PhpClass getPhpClassFromPsiReference(@NotNull ClassReference classReference) {
        PsiReference psiReference = classReference.getReference();
        if (psiReference == null) {
            return null;
        }

        PsiElement resolved = psiReference.resolve();
        if (resolved instanceof PhpClass resolvedPhpClass) {
            return resolvedPhpClass;
        }

        return null;
    }

    /**
     * Resolves the PhpClass from a MethodReference element.
     * If the element is not a MethodReference or cannot be resolved, returns null.
     *
     * @param methodReference the PSI element to check
     * @return the resolved PhpClass or null if not found
     */
    private static @Nullable PhpClass resolvePhpClassFromMethodRef(MethodReference methodReference) {
        ClassReferenceImpl classReference = getClassReferenceImplFromMethodRef(methodReference);
        if (classReference == null) {
            return null;
        }

        return getPhpClassFromPsiReference(classReference);
    }

    /**
     * Retrieves the containing PhpClass from a MethodReference element.
     * If the element is not a MethodReference or cannot be resolved, returns null.
     *
     * @param methodReference the PSI element to check
     * @return the containing PhpClass or null if not found
     */
    private static @Nullable PhpClass doGetContainingPhpClassFromMethodRef(MethodReference methodReference) {
        PsiReference reference = methodReference.getReference();
        if (reference == null) {
            return null;
        }

        PsiElement resolved = reference.resolve();
        if (!(resolved instanceof MethodImpl method)) {
            return null;
        }

        return method.getContainingClass();
    }
}

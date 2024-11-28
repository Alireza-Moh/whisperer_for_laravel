package at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
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

    public static boolean isEloquentModel(MethodReference method, Project project) {
        PhpClass eloquentModel = ClassUtils.getEloquentBaseModel(project);
        if (eloquentModel == null) {
            return false;
        }

        List<PhpClassImpl> classes = MethodUtils.resolveMethodClasses(method, project);
        for (PhpClassImpl clazz : classes) {
            if (ClassUtils.isChildOf(clazz, eloquentModel)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEloquentModel(PhpClass model, Project project) {
        PhpClass eloquentModel = ClassUtils.getEloquentBaseModel(project);
        if (eloquentModel == null) {
            return false;
        }

        return ClassUtils.isChildOf(model, eloquentModel);
    }

    public static @Nullable PhpClass getEloquentModel(MethodReference method, Project project) {
        PhpClass eloquentModel = ClassUtils.getEloquentBaseModel(project);
        if (eloquentModel == null) {
            return null;
        }

        List<PhpClassImpl> classes = MethodUtils.resolveMethodClasses(method, project);
        for (PhpClassImpl clazz : classes) {
            if (ClassUtils.isChildOf(clazz, eloquentModel)) {
                return clazz;
            }
        }
        return null;
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

    public static boolean isChildOf(PhpClass phpClass, PhpClass clazz) {
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
}

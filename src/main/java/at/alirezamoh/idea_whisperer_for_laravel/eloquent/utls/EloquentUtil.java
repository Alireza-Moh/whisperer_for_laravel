package at.alirezamoh.idea_whisperer_for_laravel.eloquent.utls;

import at.alirezamoh.idea_whisperer_for_laravel.eloquent.relation.utils.FoundedEloquentModel;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.LaravelPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.MethodUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.FunctionImpl;
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EloquentUtil {
    private EloquentUtil() {}

    @SuppressWarnings({})
    public static boolean isInsideCorrectRelationMethodMethod(PsiElement psiElement) {
        MethodReference methodReference = MethodUtils.resolveMethodReference(psiElement, 10);
        Project project = psiElement.getProject();
        MethodReference parent = getParentOfClosure(methodReference);

        if (isInsideModelQueryRelationClosure(methodReference, project)) {
            return methodReference != null &&
                isQueryRelationMethod(methodReference)
                && isQueryRelationParam(methodReference, psiElement)
                && parent != null
                && isQueryRelationMethod(parent);
        }
        else {
            PhpClass eloquentModel = getEloquentModelStart(methodReference);
            PhpClass baseEloquentModel = ClassUtils.getEloquentBaseModel(project);

            return methodReference != null
                && eloquentModel != null
                && baseEloquentModel != null
                && ClassUtils.isChildOf(eloquentModel, baseEloquentModel)
                && isQueryRelationMethod(methodReference)
                && isQueryRelationParam(methodReference, psiElement);
        }
    }

    public static boolean isTableMethod(MethodReference methodReference) {
        String methodName = methodReference.getName();

        if (methodName == null) {
            return false;
        }

        return LaravelPaths.DB_TABLE_METHODS.containsKey(methodName);
    }

    public static boolean isQueryRelationMethod(MethodReference methodReference) {
        String methodName = methodReference.getName();

        if (methodName == null) {
            return false;
        }

        return LaravelPaths.QUERY_RELATION_PARAMS.containsKey(methodName);
    }

    public static boolean isFieldIn(PsiElement element, MethodReference method, boolean allowArray) {
        return isFieldParam(method, element, allowArray) || hasFieldsInAllParams(method);
    }

    public static boolean isFieldParam(MethodReference method, PsiElement position, boolean allowArray) {
        int paramIndex = MethodUtils.findParamIndex(position, allowArray);
        return isFieldParamInCorrectMethod(method, paramIndex);
    }

    public static boolean hasFieldsInAllParams(MethodReference method) {
        return isFieldParamInCorrectMethod(method, -1);
    }

    public static boolean isFieldParamInCorrectMethod(MethodReference methodReference, int index) {
        List<Integer> paramPositions = LaravelPaths.BUILDER_METHODS.get(methodReference.getName());
        return paramPositions != null && paramPositions.contains(index);
    }

    public static boolean isTableParam(MethodReference method, PsiElement position) {
        int paramIndex = MethodUtils.findParamIndex(position, false);
        Integer paramPosition = LaravelPaths.DB_TABLE_METHODS.get(method.getName());

        return paramPosition == paramIndex;
    }

    public static boolean isQueryRelationParam(MethodReference method, PsiElement position) {
        int paramIndex = MethodUtils.findParamIndex(position, false);
        int paramPositions = LaravelPaths.QUERY_RELATION_PARAMS.get(method.getName());

        return paramPositions == paramIndex;
    }

/*    public static boolean isInsideModelQueryRelationClosure(MethodReference methodReference, Project project) {
        MethodReference parentClosure = getParentOfClosure(methodReference);
        if (parentClosure == null) {
            return false;
        }

        PhpClass baseEloquentModel = ClassUtils.getEloquentBaseModel(project);

        for (PhpClassImpl phpClass : MethodUtils.resolveMethodClasses(parentClosure, project)) {
            if (baseEloquentModel != null && ClassUtils.isChildOf(phpClass, baseEloquentModel)) {
                return true;
            }
        }

        return false;
    }*/

    public static boolean isInsideModelQueryRelationClosure(MethodReference methodReference, Project project) {
        MethodReference parentClosure = getParentOfClosure(methodReference);

        if (parentClosure != null) {
            PhpClass phpClass = getEloquentModelStart(parentClosure);
            return phpClass != null;
        }
        return false;
    }

    public static @Nullable MethodReference getParentOfClosure(MethodReference methodReference) {
        FunctionImpl function = PsiTreeUtil.getParentOfType(methodReference, FunctionImpl.class);
        MethodReference method = null;

        if (function != null) {
            method = PsiTreeUtil.getParentOfType(function, MethodReference.class);
        }

        return method;
    }

    public static @Nullable PhpClass getClassFromPhpTypedElement(PhpTypedElement element, Project project) {
        if (element.getDeclaredType() == null || element.getDeclaredType().getTypes().isEmpty()) {
            return null;
        }

        String className = element.getDeclaredType().getTypes().iterator().next();
        return PhpIndex.getInstance(project).getClassesByFQN(className).stream()
            .findFirst()
            .orElse(null);
    }

    public static @Nullable PhpClass getEloquentModelStart(MethodReference methodReference) {
        // Start with the closest MethodReference
        MethodReference currentMethod = methodReference;

        // Traverse up the tree to find the topmost method reference
        while (currentMethod != null) {
            // Attempt to get the class reference from the method
            FoundedEloquentModel resolvedClass = resolveClassFromMethod(currentMethod);
            if (resolvedClass != null) {
                return resolvedClass.model();
            }

            // Move up to the next MethodReference in the tree
            currentMethod = PsiTreeUtil.getParentOfType(currentMethod, MethodReference.class);
        }

        return null; // No root found
    }

    public static @Nullable FoundedEloquentModel getEloquentModelStart(PsiElement psiElement) {
        // Start with the closest MethodReference
        MethodReference currentMethod = PsiTreeUtil.getParentOfType(psiElement, MethodReference.class);

        // Traverse up the tree to find the topmost method reference
        while (currentMethod != null) {
            // Attempt to get the class reference from the method
            FoundedEloquentModel resolvedClass = resolveClassFromMethod(currentMethod);
            if (resolvedClass != null) {
                return resolvedClass;
            }

            // Move up to the next MethodReference in the tree
            currentMethod = PsiTreeUtil.getParentOfType(currentMethod, MethodReference.class);
        }

        return null; // No root found
    }

    private static @Nullable FoundedEloquentModel resolveClassFromMethod(MethodReference methodReference) {
        // Get class reference if directly available
        PsiElement classReference = methodReference.getClassReference();

        if (classReference instanceof ClassReferenceImpl classReferenceImpl) {
            PhpClass resolvedClass = (PhpClass) classReferenceImpl.resolve();

            if (resolvedClass != null) {
                return new FoundedEloquentModel(resolvedClass, methodReference);
            }
        }

        if (classReference instanceof VariableImpl variable) {
            PsiElement resolved = variable.resolve();

            if (resolved instanceof VariableImpl variable1) {
                MethodReference m = PsiTreeUtil.getNextSiblingOfType(variable1, MethodReference.class);
                if (m != null) {
                    return resolveClassFromMethod(m);
                }
            }
        }

        // If the class reference is a method reference itself, continue resolving
        for (PsiElement child : methodReference.getChildren()) {
            if (child instanceof MethodReference methodReference2) {
                return resolveClassFromMethod(methodReference2);
            }
        }

        return null;
    }

/*    private static @Nullable PhpClass resolveClassFromMethod(MethodReference methodReference) {
        // Get class reference if directly available
        PsiElement classReference = methodReference.getClassReference();

        if (classReference instanceof ClassReferenceImpl classReferenceImpl) {
            PhpClass resolvedClass = (PhpClass) classReferenceImpl.resolve();

            if (resolvedClass != null) {
                return resolvedClass;
            }
        }

        if (classReference instanceof VariableImpl variable) {
            PsiElement resolved = variable.resolve();

            if (resolved instanceof VariableImpl variable1) {
                MethodReference m = PsiTreeUtil.getNextSiblingOfType(variable1, MethodReference.class);
                if (m != null) {
                    return resolveClassFromMethod(m);
                }
            }
        }

        // If the class reference is a method reference itself, continue resolving
        for (PsiElement child : methodReference.getChildren()) {
            if (child instanceof MethodReference) {
                PhpClass resolvedClass = resolveClassFromMethod((MethodReference) child);
                if (resolvedClass != null) {
                    return resolvedClass;
                }
            }
        }

        return null; // No class reference found
    }*/
}

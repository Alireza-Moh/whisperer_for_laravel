package at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils;


import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.*;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MethodUtils extends PhpElementVisitor {
    public static List<PhpClassImpl> resolveMethodClasses(MethodReference method, Project project) {
        List<PhpClassImpl> classes = new ArrayList<>();
        PhpIndex phpIndex = PhpIndex.getInstance(project);

        phpIndex.completeType(project, Objects.requireNonNull(method.getClassReference()).getDeclaredType(), null).getTypes()
            .forEach(className -> collectClasses(project, className, classes));

        return classes;
    }

    public static @Nullable String resolveModelName(PsiElement element, Project project) {
        MethodReference methodReference = resolveMethodReference(element, 10);
        if (methodReference == null) {
            return null;
        }

        PhpClass eloquentModel = ClassUtils.getEloquentBaseModel(project);

        if (eloquentModel == null) {
            return null;
        }

        List<PhpClassImpl> resolvedClasses = resolveMethodClasses(methodReference, project);
        for (PhpClassImpl clazz : resolvedClasses) {
            if (ClassUtils.isChildOf(clazz, eloquentModel)) {
                return clazz.getName();
            }
        }

        return null;
    }

    public static @Nullable PhpClass getEloquentModel(PsiElement element, Project project) {
        MethodReference methodReference = resolveMethodReference(element, 10);
        PhpClass eloquentModel = ClassUtils.getEloquentBaseModel(project);

        if (eloquentModel == null || methodReference == null) {
            return null;
        }

        List<PhpClassImpl> resolvedClasses = resolveMethodClasses(methodReference, project);
        for (PhpClassImpl clazz : resolvedClasses) {
            if (ClassUtils.isChildOf(clazz, eloquentModel)) {
                return clazz;
            }
        }

        return null;
    }

    public static @Nullable MethodReference resolveMethodReference(PsiElement element, int depthLimit) {
        if (element == null || depthLimit <= 0) {
            return null;
        }

        if (element.getParent() instanceof MethodReference) {
            return (MethodReference) element.getParent();
        }

        return resolveMethodReference(element.getParent(), depthLimit - 1);
    }

    public static FunctionReference resolveFunctionReference(PsiElement element, int depthLimit) {
        if (element == null || depthLimit <= 0) {
            return null;
        }

        if (element.getParent() instanceof FunctionReference) {
            return (FunctionReference) element.getParent();
        }

        return resolveFunctionReference(element.getParent(), depthLimit - 1);
    }

    public static int findParamIndex(PsiElement element, boolean allowArray) {
        if (element == null) {
            return -1;
        }

        PsiElement parent = element.getParent();
        if (parent == null) {
            return -1;
        }

        if (parent instanceof ParameterList parameterList) {
            return ArrayUtil.indexOf(parameterList.getParameters(), element);
        } else if (allowArray && parent instanceof ArrayCreationExpressionImpl) {
            PsiElement[] children = parent.getChildren();
            for (int i = 0; i < children.length; i++) {
                if (children[i] == element) {
                    return i;
                }
            }
            return -1;
        } else {
            return findParamIndex(parent, allowArray);
        }
    }

    public static PhpClass getEloquentModelFromModelClosure(MethodReference method, Project project) {
        MethodReference parentClosure = getParentOfClosure(method);

        if (parentClosure != null && parentClosure.getClassReference() instanceof MethodReference) {
            return ClassUtils.getEloquentModel(parentClosure, project);
        }
        return null;
    }

/*    public static boolean isInsideModelQueryClosure(MethodReference method, Project project) {
        FunctionImpl function = PsiTreeUtil.getParentOfType(method, FunctionImpl.class);

        boolean isRelated = ClassUtils.isLaravelRelatedClass(method, project);

        return function != null && isRelated;
    }*/

/*    public static MethodReference getParentOfClosure(MethodReference methodReference) {
        FunctionImpl function = PsiTreeUtil.getParentOfType(methodReference, FunctionImpl.class);
        return function != null ? PsiTreeUtil.getParentOfType(methodReference, MethodReference.class) : null;
    }

    public static void findMethodsInTree(PsiElement root, List<MethodReference> list) {
        if (root == null) {
            return;
        }

        for (PsiElement child : root.getChildren()) {
            if (child instanceof MethodReference) {
                list.add((MethodReference) child);
                findMethodsInTree(child, list);
            }
        }
    }*/

    public static boolean isInsideModelQueryClosure(MethodReference methodReference, Project project) {
        MethodReference parentClosure = getParentOfClosure(methodReference);
        if (parentClosure == null) {
            return false;
        }

        PhpClass baseEloquentModel = ClassUtils.getEloquentBaseModel(project);

        for (PhpClassImpl phpClass : resolveMethodClasses(parentClosure, project)) {
            if (baseEloquentModel != null && ClassUtils.isChildOf(phpClass, baseEloquentModel)) {
                return true;
            }
        }

        return false;
    }

    public static @Nullable MethodReference getParentOfClosure(MethodReference methodReference) {
        return PsiTreeUtil.getParentOfType(methodReference, FunctionImpl.class) != null
            ? PsiTreeUtil.getParentOfType(methodReference, MethodReference.class)
            : null;
    }

    public static @Nullable PhpClassImpl getClass(PhpExpression typedElement, Project project) {
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

    public static boolean isModelReference(MethodReference methodReference, Project project) {
        PsiElement firstChild = methodReference.getFirstPsiChild();

        PhpClass eloquentBaseModel = ClassUtils.getEloquentBaseModel(project);

        if (firstChild instanceof ClassReferenceImpl) {
            PhpClassImpl clazz = getClassFromTypedElement((ClassReferenceImpl) methodReference.getFirstChild(), project);
            return clazz != null && eloquentBaseModel != null && ClassUtils.isChildOf(clazz, eloquentBaseModel);
        } else if (firstChild instanceof VariableImpl) {
            PhpClassImpl clazz = getClassFromTypedElement((VariableImpl) methodReference.getFirstChild(), project);
            return clazz != null && eloquentBaseModel != null && ClassUtils.isChildOf(clazz, eloquentBaseModel);
        } else {
            return false;
        }
    }

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

    public static boolean isRelation(MethodReference methodReference, Project project) {
        List<PhpClassImpl> resolvedClasses = MethodUtils.resolveMethodClasses(methodReference, project);
        PhpClass relationClass = ClassUtils.getClassByFQN(project, LaravelPaths.LaravelClasses.Relation);

        if (relationClass == null) {
            return false;
        }

        return resolvedClasses.stream().anyMatch(phpClass -> ClassUtils.isChildOf(phpClass, relationClass));
    }

    public static boolean isJoinClause(MethodReference methodReference, Project project) {
        List<PhpClassImpl> resolvedClasses = MethodUtils.resolveMethodClasses(methodReference, project);
        PhpClass joinClass = ClassUtils.getClassByFQN(project, LaravelPaths.LaravelClasses.JoinClause);

        if (joinClass == null) {
            return false;
        }

        return resolvedClasses.stream().anyMatch(phpClass -> ClassUtils.isChildOf(phpClass, joinClass));
    }

    public static boolean isJoinOrRelation(PhpClassImpl phpClass) {
        PhpClass joinClass = ClassUtils.getClassByFQN(phpClass.getProject(), LaravelPaths.LaravelClasses.JoinClause);
        PhpClass relationClass = ClassUtils.getClassByFQN(phpClass.getProject(), LaravelPaths.LaravelClasses.Relation);

        return (joinClass != null && ClassUtils.isChildOf(phpClass, joinClass))
            || (relationClass != null && ClassUtils.isChildOf(phpClass, relationClass));
    }

    public static boolean isDumbMode(Project project) {
        return com.intellij.openapi.project.DumbService.isDumb(project);
    }

    private static void collectClasses(Project project, String className, List<PhpClassImpl> classes) {
        PhpIndex phpIndex = PhpIndex.getInstance(project);
        for (PhpClass phpClass : phpIndex.getClassesByFQN(className)) {
            if (phpClass instanceof PhpClassAliasImpl aliasClass) {
                if (aliasClass.getOriginal() != null) {
                    classes.add((PhpClassImpl) aliasClass.getOriginal());
                }
            } else if (phpClass instanceof PhpClassImpl) {
                classes.add((PhpClassImpl) phpClass);
            }
        }
    }
}

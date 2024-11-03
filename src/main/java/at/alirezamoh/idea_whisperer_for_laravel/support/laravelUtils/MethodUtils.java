package at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MethodUtils extends PhpElementVisitor {
    public static List<PhpClassImpl> resolveMethodClasses(MethodReference method, Project project) {
        List<PhpClassImpl> classes = new ArrayList<>();
        PhpIndex phpIndex = PhpIndex.getInstance(project);

        PhpExpression m = method.getClassReference();
        PhpType s = method.getClassReference().getDeclaredType();
        phpIndex.completeType(project, method.getClassReference().getDeclaredType(), null).getTypes()
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

        if (eloquentModel == null) {
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

    public static MethodReference resolveMethodReference(PsiElement element, int depthLimit) {
        if (element == null || depthLimit <= 0) {
            return null;
        }

        if (element.getParent() instanceof MethodReference) {
            ProgressManager.checkCanceled();
            return (MethodReference) element.getParent();
        }

        return resolveMethodReference(element.getParent(), depthLimit - 1);
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

    public static boolean isColumnIn(PsiElement element, MethodReference method, boolean allowArray) {
        return isColumnParam(method, element, allowArray) || hasColumnsInAllParams(method);
    }

    public static boolean isColumnParam(MethodReference method, PsiElement position, boolean allowArray) {
        int paramIndex = findParamIndex(position, allowArray);
        return isColumnParam(method, paramIndex);
    }

    public static boolean isTableParam(MethodReference method, PsiElement position) {
        int paramIndex = findParamIndex(position, false);
        return isTableParam(method, paramIndex);
    }

    public static boolean isQueryRelationParam(MethodReference method, PsiElement position) {
        int paramIndex = findParamIndex(position, false);
        return isQueryRelationParam(method, paramIndex);
    }

    public static boolean isColumnParam(MethodReference methodReference, int index) {
        List<Integer> paramPositions = LaravelPaths.BUILDER_METHODS.get(methodReference.getName());
        return paramPositions != null && paramPositions.contains(index);
    }

    public static boolean isTableParam(MethodReference methodReference, int index) {
        List<Integer> paramPositions = LaravelPaths.DB_TABLE_METHODS.get(methodReference.getName());
        return paramPositions != null && paramPositions.contains(index);
    }

    public static boolean isQueryRelationParam(MethodReference methodReference, int index) {
        List<Integer> paramPositions = LaravelPaths.QUERY_RELATION_PARAMS.get(methodReference.getName());
        return paramPositions != null && paramPositions.contains(index);
    }

    public static boolean hasColumnsInAllParams(MethodReference method) {
        return isColumnParam(method, -1);
    }

    public static int findParamIndex(PsiElement element) {
        return findParamIndex(element, false);
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
            int s = ArrayUtil.indexOf(parameterList.getParameters(), element);
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

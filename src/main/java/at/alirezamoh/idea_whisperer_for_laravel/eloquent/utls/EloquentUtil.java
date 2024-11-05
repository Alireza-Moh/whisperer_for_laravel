package at.alirezamoh.idea_whisperer_for_laravel.eloquent.utls;

import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.LaravelPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.MethodUtils;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;

import java.util.List;

public class EloquentUtil {
    private EloquentUtil() {}

    public static boolean isInsideCorrectRelationMethodMethod(PsiElement psiElement) {
        MethodReference methodReference = MethodUtils.resolveMethodReference(psiElement, 10);

        return methodReference != null
            && ClassUtils.isLaravelRelatedClass(methodReference, psiElement.getProject())
            && isQueryRelationMethod(methodReference)
            && isQueryRelationParam(methodReference, psiElement);
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
        return isParamInCorrectMethod(method, paramIndex);
    }

    public static boolean hasFieldsInAllParams(MethodReference method) {
        return isParamInCorrectMethod(method, -1);
    }

    public static boolean isParamInCorrectMethod(MethodReference methodReference, int index) {
        List<Integer> paramPositions = LaravelPaths.BUILDER_METHODS.get(methodReference.getName());
        return paramPositions != null && paramPositions.contains(index);
    }

    public static boolean isTableParam(MethodReference method, PsiElement position) {
        int paramIndex = MethodUtils.findParamIndex(position, false);
        List<Integer> paramPositions = LaravelPaths.DB_TABLE_METHODS.get(method.getName());

        return paramPositions != null && paramPositions.contains(paramIndex);
    }

    public static boolean isQueryRelationParam(MethodReference method, PsiElement position) {
        int paramIndex = MethodUtils.findParamIndex(position, false);
        List<Integer> paramPositions = LaravelPaths.QUERY_RELATION_PARAMS.get(method.getName());

        return paramPositions != null && paramPositions.contains(paramIndex);
    }
}

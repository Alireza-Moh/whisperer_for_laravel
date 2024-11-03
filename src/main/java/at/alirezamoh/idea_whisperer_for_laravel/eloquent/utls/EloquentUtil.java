package at.alirezamoh.idea_whisperer_for_laravel.eloquent.utls;

import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.MethodUtils;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;

public class EloquentUtil {
    private EloquentUtil() {}

    public static boolean isInsideCorrectRelationMethodMethod(PsiElement psiElement) {
        MethodReference methodReference = MethodUtils.resolveMethodReference(psiElement, 10);

        return methodReference != null
            && ClassUtils.isLaravelRelatedClass(methodReference, psiElement.getProject())
            && MethodUtils.isQueryRelationMethod(methodReference)
            && MethodUtils.isQueryRelationParam(methodReference, psiElement);
    }
}

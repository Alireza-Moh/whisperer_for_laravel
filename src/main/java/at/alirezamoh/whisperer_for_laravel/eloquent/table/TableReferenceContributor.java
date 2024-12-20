package at.alirezamoh.whisperer_for_laravel.eloquent.table;

import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.LaravelPaths;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class TableReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression.class).withParent(ParameterList.class),
            new PsiReferenceProvider() {

                @Override
                public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
                    Project project = psiElement.getProject();

                    if (!FrameworkUtils.isLaravelProject(project) && FrameworkUtils.isLaravelFrameworkNotInstalled(project)) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    if (isInsideCorrectMethod(psiElement)) {
                        PsiElement element = psiElement.getOriginalElement();
                        String text = element.getText();

                        return new PsiReference[]{
                            new TableReference(element, new TextRange(PsiUtil.getStartOffset(text), PsiUtil.getEndOffset(text)))
                        };
                    }

                    return new PsiReference[0];
                }
            }
        );
    }

    private boolean isInsideCorrectMethod(PsiElement psiElement) {
        MethodReference methodReference = MethodUtils.resolveMethodReference(psiElement, 10);

        return methodReference != null && ClassUtils.isLaravelRelatedClass(methodReference, psiElement.getProject())
            && isTableMethod(methodReference)
            && isTableParam(methodReference, psiElement);
    }

    private boolean isTableMethod(MethodReference methodReference) {
        String methodName = methodReference.getName();

        if (methodName == null) {
            return false;
        }

        return LaravelPaths.DB_TABLE_METHODS.containsKey(methodName);
    }

    private boolean isTableParam(MethodReference method, PsiElement position) {
        int paramIndex = MethodUtils.findParamIndex(position, false);
        Integer paramPosition = LaravelPaths.DB_TABLE_METHODS.get(method.getName());

        return paramPosition == paramIndex;
    }
}

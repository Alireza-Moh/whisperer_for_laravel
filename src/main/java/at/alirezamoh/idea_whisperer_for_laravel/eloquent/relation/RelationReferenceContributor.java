package at.alirezamoh.idea_whisperer_for_laravel.eloquent.relation;

import at.alirezamoh.idea_whisperer_for_laravel.eloquent.utls.EloquentUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.MethodUtils;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class RelationReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression.class),
            new PsiReferenceProvider() {

                @Override
                public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
                    if (MethodUtils.isDumbMode(psiElement.getProject())) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    if (psiElement instanceof StringLiteralExpression && EloquentUtil.isInsideCorrectRelationMethodMethod(psiElement)) {

                        return new PsiReference[]{
                            new RelationReference(psiElement)
                        };
                    }
                    return PsiReference.EMPTY_ARRAY;
                }
            }
        );
    }
}

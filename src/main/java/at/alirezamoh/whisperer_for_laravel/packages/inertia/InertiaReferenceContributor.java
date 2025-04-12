package at.alirezamoh.whisperer_for_laravel.packages.inertia;

import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class InertiaReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
            psiElement(StringLiteralExpression.class).withParent(psiElement(ParameterList.class)),
            new PsiReferenceProvider() {

                @Override
                public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
                    if (InertiaUtil.shouldNotCompleteOrNavigate(psiElement.getProject())) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    if (!(psiElement instanceof StringLiteralExpression stringLiteralExpression)) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    if (InertiaMethodValidator.isInsideCorrectMethod(psiElement)) {
                        return new PsiReference[]{new InertiaReference(
                            stringLiteralExpression,
                            new TextRange(
                                PsiElementUtils.getStartOffset(stringLiteralExpression),
                                PsiElementUtils.getEndOffset(stringLiteralExpression)
                            )
                        )};
                    }

                    return PsiReference.EMPTY_ARRAY;
                }
            }
        );
    }
}

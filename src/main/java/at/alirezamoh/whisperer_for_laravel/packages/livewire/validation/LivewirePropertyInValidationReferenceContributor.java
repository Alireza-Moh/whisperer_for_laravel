package at.alirezamoh.whisperer_for_laravel.packages.livewire.validation;

import at.alirezamoh.whisperer_for_laravel.request.requestField.util.RequestFieldUtils;
import at.alirezamoh.whisperer_for_laravel.request.validation.util.RuleValidationUtil;
import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.ArrayHashElementImpl;
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl;
import org.jetbrains.annotations.NotNull;

/**
 * This class allows the IDE to recognize and resolve livewire component property inside the validation method
 */
public class LivewirePropertyInValidationReferenceContributor extends PsiReferenceContributor {
    /**
     * Registers the reference provider
     * @param psiReferenceRegistrar The PSI reference registrar
     */
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression.class),
            new PsiReferenceProvider() {
                @Override
                public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
                    Project project = psiElement.getProject();

                    if (!PluginUtils.isLaravelProject(project) && PluginUtils.isLaravelFrameworkNotInstalled(project)) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    if (!(psiElement instanceof StringLiteralExpression stringLiteralExpression)) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    if(isInsideLivewireValidateMethod(psiElement, project) || isInsideRulesMethod(psiElement))
                    {
                        return new PsiReference[]{
                            new LivewirePropertyInValidationReference(
                                stringLiteralExpression,
                                new TextRange(
                                    PsiElementUtils.getStartOffset(stringLiteralExpression),
                                    PsiElementUtils.getEndOffset(stringLiteralExpression)
                                )
                            )
                        };
                    }
                    return PsiReference.EMPTY_ARRAY;
                }
            }
        );
    }

    private boolean isInsideLivewireValidateMethod(PsiElement psiElement, Project project) {
        MethodReference methodReference = MethodUtils.resolveMethodReference(psiElement, 10);

        return methodReference != null
            && PhpClassUtils.isCorrectRelatedClass(methodReference, project, "\\Livewire\\Component")
            && RequestFieldUtils.VALIDATION_METHODS.contains(methodReference.getName())
            && RuleValidationUtil.isRuleParam(methodReference, psiElement)
            && isInsideArrayKey(psiElement);
    }

    private boolean isInsideArrayKey(PsiElement psiElement) {
        return PsiElementUtils.isAssocArray(psiElement, 10) && isInArrayKey(psiElement, 10);
    }

    /**
     * Checks if the given PSI element is a key in an array [key => value]
     *
     * @param element  The PSI element to check
     * @param maxDepth Maximum number of parents to traverse up the PSI tree
     * @return true or false
     */
    private boolean isInArrayKey(PsiElement element, int maxDepth) {
        PsiElement currentElement = element;
        int currentDepth = 0;

        while (currentElement != null && currentDepth < maxDepth) {
            if (currentElement instanceof ArrayHashElementImpl arrayHashElement) {
                if (arrayHashElement.getKey() == element) {
                    return true;
                }
            }

            currentElement = currentElement.getParent();
            currentDepth++;
        }

        return false;
    }

    /**
     * Checks if the PSI element is inside a MethodImpl with the name 'rules'
     */
    private boolean isInsideRulesMethod(PsiElement psiElement) {
        MethodImpl methodCall = PsiTreeUtil.getParentOfType(psiElement, MethodImpl.class);

        return methodCall != null
            && methodCall.getName().equals("rules")
            && isInsideArrayKey(psiElement);
    }
}

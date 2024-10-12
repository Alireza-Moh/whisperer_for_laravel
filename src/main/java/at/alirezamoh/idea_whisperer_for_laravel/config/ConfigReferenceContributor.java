package at.alirezamoh.idea_whisperer_for_laravel.config;


import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.openapi.externalSystem.autoimport.ProjectStatus;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

/**
 * Contributes references to Laravel config keys in the PSI tree
 */
public class ConfigReferenceContributor extends PsiReferenceContributor {
    /**
     * The namespace of the `Config` facade
     */
    private final String CONFIG_NAMESPACE = "\\Illuminate\\Support\\Facades\\Config";

    /**
     * The names of the methods in the `Config` facade that can reference config keys
     */
    private final String[] CONFIG_METHODS = {"get", "has"} ;

    /**
     * The name of the `config` helper function
     */
    private final String CONFIG_FUNCTION = "config" ;


    /**
     * Registers the reference provider for config keys
     * @param psiReferenceRegistrar The PSI reference registrar
     */
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
                PlatformPatterns.psiElement(StringLiteralExpression.class),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
                        if (isInsideConfigHelperMethod(psiElement) && psiElement instanceof StringLiteralExpression stringLiteralExpression)
                        {
                            String text = stringLiteralExpression.getText();

                            return new PsiReference[]{
                                    new ConfigReference(
                                            psiElement,
                                            new TextRange(PsiUtil.getStartOffset(text), PsiUtil.getEndOffset(text))
                                    )
                            };
                        }
                        return PsiReference.EMPTY_ARRAY;
                    }
                }
        );
    }

    /**
     * Checks if the given PSI element is inside a method or function that can reference config keys
     * @param psiElement The PSI element to check
     * @return           True if the element is inside a relevant method or function, false otherwise
     */
    private boolean isInsideConfigHelperMethod(@NotNull PsiElement psiElement) {
        return PsiUtil.isInsideFunction(psiElement, CONFIG_FUNCTION)
            || PsiUtil.isInsideMethod(psiElement, CONFIG_METHODS, CONFIG_NAMESPACE);
    }
}

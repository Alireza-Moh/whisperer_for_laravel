package at.alirezamoh.idea_whisperer_for_laravel.gate;

import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class GateReferenceContributor extends PsiReferenceContributor {
    private final String[] GATE_METHODS = {"allows", "denies"} ;

    private final String GATE_NAMESPACE = "\\Illuminate\\Support\\Facades\\Gate";

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression.class),
            new PsiReferenceProvider() {

                @Override
                public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                    if (
                        PsiUtil.isInsideMethod(element, GATE_METHODS, GATE_NAMESPACE)
                        && element instanceof StringLiteralExpression stringLiteralExpression
                    ) {
                        String text = stringLiteralExpression.getText();

                        return new PsiReference[]{
                            new GateReference(
                                element,
                                new TextRange(PsiUtil.getStartOffset(text), PsiUtil.getEndOffset(text))
                            )
                        };
                    }

                    return PsiReference.EMPTY_ARRAY;
                }
            }
        );
    }
}

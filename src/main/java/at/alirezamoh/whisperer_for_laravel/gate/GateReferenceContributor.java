package at.alirezamoh.whisperer_for_laravel.gate;

import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GateReferenceContributor extends PsiReferenceContributor {
    public static final String GATE = "\\Illuminate\\Support\\Facades\\Gate";

    public static Map<String, Integer> GATE_METHODS = new HashMap<>() {{
        put("allows", 0);
        put("denies", 0);
        put("any", 0);
        put("none", 0);
        put("authorize", 0);
        put("check", 0);
        put("inspect", 0);
        put("has", 0);
    }};

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression.class),
            new PsiReferenceProvider() {

                @Override
                public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                    Project project = element.getProject();

                    if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    if (!(element instanceof StringLiteralExpression stringLiteralExpression)) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    if (isInsideGateMethod(element, element.getProject())) {
                        return new PsiReference[]{
                            new GateReference(
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

    /**
     * Checks if the given PSI element is inside a recognized Gate method call (e.g., Gate::allows or Gate::denies)
     *
     * @param psiElement The PSI element to inspect
     * @param project    The current project
     * @return true or false
     */
    private boolean isInsideGateMethod(PsiElement psiElement, Project project) {
        MethodReference methodReference = MethodUtils.resolveMethodReference(psiElement, 10);

        return methodReference != null
            && PhpClassUtils.isCorrectRelatedClass(methodReference, project, GATE)
            && isGateParam(methodReference, psiElement);
    }

    /**
     * Checks if the specified PSI element corresponds to the parameter that Gate methods expect
     * (based on a predefined map of method names to parameter indices)
     *
     * @param method   The Gate method reference
     * @param position The PSI element representing the parameter value
     * @return true or false
     */
    public boolean isGateParam(MethodReference method, PsiElement position) {
        Integer expectedParamIndex = GATE_METHODS.get(method.getName());

        if (expectedParamIndex == null) {
            return false;
        }
        return expectedParamIndex == MethodUtils.findParamIndex(position, false);
    }
}

package at.alirezamoh.idea_whisperer_for_laravel.gate;

import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.MethodUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GateReferenceContributor extends PsiReferenceContributor {
    public static Map<String, List<Integer>> GATE_METHODS = new HashMap<>() {{
        put("allows", List.of(0));
        put("denies", List.of(0));
        put("any", List.of(0));
        put("none", List.of(0));
        put("authorize", List.of(0));
        put("check", List.of(0));
        put("inspect", List.of(0));
    }};

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression.class),
            new PsiReferenceProvider() {

                @Override
                public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                    if (element instanceof StringLiteralExpression && isInsideGateMethod(element, element.getProject())) {
                        String text = element.getText();

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

    private boolean isInsideGateMethod(PsiElement psiElement, Project project) {
        MethodReference methodReference = MethodUtils.resolveMethodReference(psiElement, 10);

        return methodReference != null &&
            ClassUtils.isLaravelRelatedClass(methodReference, project)
            && isGateParam(methodReference, psiElement);
    }

    public boolean isGateParam(MethodReference method, PsiElement position) {
        int paramIndex = MethodUtils.findParamIndex(position, false);
        List<Integer> paramPositions = GATE_METHODS.get(method.getName());

        return paramPositions != null && paramPositions.contains(paramIndex);
    }
}

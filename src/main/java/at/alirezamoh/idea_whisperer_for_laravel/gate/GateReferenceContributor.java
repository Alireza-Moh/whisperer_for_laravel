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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GateReferenceContributor extends PsiReferenceContributor {
    public static final List<String> GATE = new ArrayList<>() {{
        add("\\Illuminate\\Support\\Facades\\Gate");
    }};

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
                    if (isInsideGateMethod(element, element.getProject())) {
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

        return methodReference != null
            && ClassUtils.isCorrectRelatedClass(methodReference, project, GATE)
            && isGateParam(methodReference, psiElement);
    }

    public boolean isGateParam(MethodReference method, PsiElement position) {
        Integer expectedParamIndex = GATE_METHODS.get(method.getName());

        if (expectedParamIndex == null) {
            return false;
        }

        return GATE_METHODS.get(method.getName()) == MethodUtils.findParamIndex(position, false);
    }
}

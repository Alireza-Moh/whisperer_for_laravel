package at.alirezamoh.idea_whisperer_for_laravel.config;


import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.MethodUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Contributes references to Laravel config keys in the PSI tree
 */
public class ConfigReferenceContributor extends PsiReferenceContributor {
    /**
     * The namespace of the `Config` facade
     */
    private final List<String> CONFIG = new ArrayList<>() {{
        add("\\Illuminate\\Support\\Facades\\Config");
    }};

    /**
     * The names of the methods in the `Config` facade that can reference config keys
     */
    public static Map<String, List<Integer>> CONFIG_METHODS = new HashMap<>() {{
        put("get", List.of(0));
        put("has", List.of(0));
        put("config", List.of(0));
        put("array", List.of(0));
        put("boolean", List.of(0));
        put("float", List.of(0));
        put("integer", List.of(0));
        put("string", List.of(0));
        put("getMany", List.of(0));
        put("set", List.of(0));
        put("prepend", List.of(0));
    }};

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
                        if (FrameworkUtils.isLaravelFrameworkNotInstalled(psiElement.getProject())) {
                            return PsiReference.EMPTY_ARRAY;
                        }

                        boolean a = isInsideConfigHelperMethod(psiElement);
                        if (isInsideConfigHelperMethod(psiElement))
                        {
                            String text = psiElement.getText();

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
        MethodReference method = MethodUtils.resolveMethodReference(psiElement, 10);
        FunctionReference function = MethodUtils.resolveFunctionReference(psiElement, 10);
        Project project = psiElement.getProject();

        return (
            method != null
                && isConfigParam(method, psiElement)
                && ClassUtils.isCorrectRelatedClass(method, project, CONFIG)
            )
            || (
                function != null
                    && isConfigParam(function, psiElement)
                    && Objects.equals(function.getName(), "config")
            );
    }

    /**
     * General method to check if the given reference and position match the config parameter criteria
     * @param reference The method or function reference
     * @param position The PSI element position
     * @return True or false
     */
    private boolean isConfigParam(PsiElement reference, PsiElement position) {
        int paramIndex = MethodUtils.findParamIndex(position, false);
        String referenceName = (reference instanceof MethodReference)
                ? ((MethodReference) reference).getName()
                : ((FunctionReference) reference).getName();

        List<Integer> paramPositions = CONFIG_METHODS.get(referenceName);

        return paramPositions != null && paramPositions.contains(paramIndex);
    }
}

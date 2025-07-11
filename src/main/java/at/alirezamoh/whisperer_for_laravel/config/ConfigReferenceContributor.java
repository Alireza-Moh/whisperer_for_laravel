package at.alirezamoh.whisperer_for_laravel.config;


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
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Contributes references to Laravel config keys in the PSI tree
 */
public class ConfigReferenceContributor extends PsiReferenceContributor {
    /**
     * The namespace of the `Config` facade
     */
    private final String CONFIG = "\\Illuminate\\Support\\Facades\\Config";

    /**
     * The names of the methods in the `Config` facade that can reference config keys
     */
    public static Map<String, Integer> CONFIG_METHODS = new HashMap<>() {{
        put("get", 0);
        put("has", 0);
        put("config", 0);
        put("array", 0);
        put("boolean", 0);
        put("float", 0);
        put("integer", 0);
        put("string", 0);
        put("getMany", 0);
        put("set", 0);
        put("prepend", 0);
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
                        Project project = psiElement.getProject();

                        if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
                            return PsiReference.EMPTY_ARRAY;
                        }

                        if (!(psiElement instanceof StringLiteralExpression stringLiteralExpression)) {
                            return PsiReference.EMPTY_ARRAY;
                        }

                        if (isInsideConfigHelperMethod(psiElement))
                        {
                            return new PsiReference[]{
                                    new ConfigReference(
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
     * Checks if the given PSI element is inside a method or function that can reference config keys
     * @param psiElement The PSI element to check
     * @return           True if the element is inside a relevant method or function, false otherwise
     */
    private boolean isInsideConfigHelperMethod(@NotNull PsiElement psiElement) {
        MethodReference method = MethodUtils.resolveMethodReference(psiElement, 10);
        FunctionReferenceImpl function = MethodUtils.resolveFunctionReference(psiElement, 5);
        Project project = psiElement.getProject();

        return (
            method != null
                && isConfigParam(method, psiElement)
                && PhpClassUtils.isCorrectRelatedClass(method, project, CONFIG)
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
        String referenceName = (reference instanceof MethodReference)
                ? ((MethodReference) reference).getName()
                : ((FunctionReferenceImpl) reference).getName();

        if (referenceName == null) {
            return false;
        }

        Integer expectedParamIndex = CONFIG_METHODS.get(referenceName);
        if (expectedParamIndex == null) {
            return false;
        }

        return MethodUtils.findParamIndex(position, false) == expectedParamIndex;
    }
}

package at.alirezamoh.idea_whisperer_for_laravel.inertia;

import at.alirezamoh.idea_whisperer_for_laravel.blade.BladeReference;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.MethodUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class InertiaReferenceContributor extends PsiReferenceContributor {
    /**
     * The names of the methods in the 'View' facade that can reference Blade files
     */
    public static Map<String, List<Integer>> Inertia_METHODS = new HashMap<>() {{
        put("render", List.of(0));
    }};

    /**
     * The FQN of the 'Route' facade
     */
    private final String INERTIA = "\\Inertia\\Inertia";

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
            psiElement(StringLiteralExpression.class).withParent(psiElement(ParameterList.class)),
            new PsiReferenceProvider() {

                @Override
                public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
                    if (FrameworkUtils.isLaravelFrameworkNotInstalled(psiElement.getProject())) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    if (isInsideViewMethods(psiElement)) {
                        String text = psiElement.getText();

                        return new PsiReference[]{new InertiaReference(
                            psiElement,
                            new TextRange(PsiUtil.getStartOffset(text), PsiUtil.getEndOffset(text))
                        )};
                    }

                    return PsiReference.EMPTY_ARRAY;
                }
            }
        );
    }

    /**
     * Checks if the given PSI element is inside inertia method
     * @param psiElement The PSI element to check
     * @return True or false
     */
    private boolean isInsideViewMethods(@NotNull PsiElement psiElement) {
        MethodReference method = MethodUtils.resolveMethodReference(psiElement, 10);
        Project project = psiElement.getProject();

        return method != null
            && isPageParam(method, psiElement)
            && isInertiaMethod(method, project);
    }

    /**
     * General method to check if the given reference and position match the view parameter criteria
     * @param reference The method or function reference
     * @param position The PSI element position
     * @return True or false
     */
    private boolean isPageParam(PsiElement reference, PsiElement position) {
        int paramIndex = MethodUtils.findParamIndex(position, false);
        String referenceName = (reference instanceof MethodReference)
            ? ((MethodReference) reference).getName()
            : ((FunctionReference) reference).getName();

        List<Integer> paramPositions = Inertia_METHODS.get(referenceName);

        return paramPositions != null && paramPositions.contains(paramIndex);
    }

    /**
     * Checks if the given method reference is a view or route method
     * @param methodReference The method reference
     * @param project The project context
     * @return True or false
     */
    private boolean isInertiaMethod(MethodReference methodReference, Project project) {
        List<PhpClassImpl> resolvedClasses = MethodUtils.resolveMethodClasses(methodReference, project);

        PhpClass inertiaClass = ClassUtils.getClassByFQN(project, INERTIA);

        return inertiaClass != null
            && resolvedClasses.stream().anyMatch(clazz -> ClassUtils.isChildOf(clazz, inertiaClass));
    }
}

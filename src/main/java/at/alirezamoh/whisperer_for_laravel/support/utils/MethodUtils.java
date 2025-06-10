package at.alirezamoh.whisperer_for_laravel.support.utils;


import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.*;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MethodUtils extends PhpElementVisitor {
    /**
     * Resolves the possible PHP classes for a given method
     *
     * @param method  The method reference whose classes should be resolved
     * @param project The current project
     * @return A list of resolved {@link PhpClassImpl} objects
     */
    public static List<PhpClassImpl> resolveMethodClasses(MethodReference method, Project project) {
        List<PhpClassImpl> classes = new ArrayList<>();
        PhpIndex phpIndex = PhpIndex.getInstance(project);

        phpIndex.completeType(project, Objects.requireNonNull(method.getClassReference()).getDeclaredType(), null).getTypes()
            .forEach(className -> collectClasses(project, className, classes));

        return classes;
    }

    /**
     * Recursively searches up the PSI tree to find the nearest {@link MethodReference}
     *
     * @param element    The starting PSI element
     * @param depthLimit Stop searching after a certain depth
     * @return The found {@link MethodReference}, or {@code null} if not found
     */
    public static @Nullable MethodReference resolveMethodReference(PsiElement element, int depthLimit) {
        if (element == null || depthLimit <= 0) {
            return null;
        }

        if (element.getParent() instanceof MethodReference) {
            return (MethodReference) element.getParent();
        }

        return resolveMethodReference(element.getParent(), depthLimit - 1);
    }

    /**
     * Recursively searches up the PSI tree to find the nearest {@link MethodImpl}
     *
     * @param element    The starting PSI element
     * @param depthLimit Stop searching after a certain depth
     * @return The found {@link MethodImpl}, or {@code null} if not found
     */
    public static @Nullable MethodImpl resolveMethodImpl(PsiElement element, int depthLimit) {
        if (element == null || depthLimit <= 0) {
            return null;
        }

        if (element.getParent() instanceof MethodImpl) {
            return (MethodImpl) element.getParent();
        }

        return resolveMethodImpl(element.getParent(), depthLimit - 1);
    }

    /**
     * Recursively searches up the PSI tree to find the nearest {@link FunctionReference}.
     *
     * @param element    The starting PSI element
     * @param depthLimit Stop searching after a certain depth
     * @return The found {@link FunctionReference}, or {@code null} if not found
     */
    public static @Nullable FunctionReference resolveFunctionReference(PsiElement element, int depthLimit) {
        if (element == null || depthLimit <= 0) {
            return null;
        }

        if (element.getParent() instanceof FunctionReference) {
            return (FunctionReference) element.getParent();
        }

        return resolveFunctionReference(element.getParent(), depthLimit - 1);
    }

    /**
     * Determines the index of a parameter within a {@link ParameterList} or
     * {@link ArrayCreationExpressionImpl}, optionally allowing arrays
     *
     * @param element     The PSI element to locate
     * @param allowArray  Whether array elements should also be considered as parameters
     * @return The parameter index, or {@code -1} if not found
     */
    public static int findParamIndex(PsiElement element, boolean allowArray) {
        if (element == null) {
            return -1;
        }

        PsiElement parent = element.getParent();
        if (parent == null) {
            return -1;
        }

        if (parent instanceof ParameterList parameterList) {
            return ArrayUtil.indexOf(parameterList.getParameters(), element);
        } else if (allowArray && parent instanceof ArrayCreationExpressionImpl) {
            PsiElement[] children = parent.getChildren();
            for (int i = 0; i < children.length; i++) {
                if (children[i] == element) {
                    return i;
                }
            }
            return -1;
        } else {
            return findParamIndex(parent, allowArray);
        }
    }

    /**
     * Resolves php classes to actual {@link PhpClassImpl} instances
     * accounting for aliases by using their original references if available
     *
     * @param project   The current project
     * @param className The FQN to resolve
     * @param classes   A list to which resolved classes are added
     */
    private static void collectClasses(Project project, String className, List<PhpClassImpl> classes) {
        PhpIndex phpIndex = PhpIndex.getInstance(project);
        for (PhpClass phpClass : phpIndex.getClassesByFQN(className)) {
            if (phpClass instanceof PhpClassAliasImpl aliasClass) {
                if (aliasClass.getOriginal() != null) {
                    classes.add((PhpClassImpl) aliasClass.getOriginal());
                }
            } else if (phpClass instanceof PhpClassImpl) {
                classes.add((PhpClassImpl) phpClass);
            }
        }
    }
}

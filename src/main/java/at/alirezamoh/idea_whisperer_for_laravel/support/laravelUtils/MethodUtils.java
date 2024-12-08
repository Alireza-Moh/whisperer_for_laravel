package at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils;


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
    public static List<PhpClassImpl> resolveMethodClasses(MethodReference method, Project project) {
        List<PhpClassImpl> classes = new ArrayList<>();
        PhpIndex phpIndex = PhpIndex.getInstance(project);

        phpIndex.completeType(project, Objects.requireNonNull(method.getClassReference()).getDeclaredType(), null).getTypes()
            .forEach(className -> collectClasses(project, className, classes));

        return classes;
    }

    public static @Nullable MethodReference resolveMethodReference(PsiElement element, int depthLimit) {
        if (element == null || depthLimit <= 0) {
            return null;
        }

        if (element.getParent() instanceof MethodReference) {
            return (MethodReference) element.getParent();
        }

        return resolveMethodReference(element.getParent(), depthLimit - 1);
    }

    public static FunctionReference resolveFunctionReference(PsiElement element, int depthLimit) {
        if (element == null || depthLimit <= 0) {
            return null;
        }

        if (element.getParent() instanceof FunctionReference) {
            return (FunctionReference) element.getParent();
        }

        return resolveFunctionReference(element.getParent(), depthLimit - 1);
    }

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

    public static boolean isDumbMode(Project project) {
        return com.intellij.openapi.project.DumbService.isDumb(project);
    }

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

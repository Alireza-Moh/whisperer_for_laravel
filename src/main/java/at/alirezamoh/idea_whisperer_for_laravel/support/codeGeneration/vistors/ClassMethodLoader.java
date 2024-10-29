package at.alirezamoh.idea_whisperer_for_laravel.support.codeGeneration.vistors;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables.Method;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClassMethodLoader {
    private Project project;

    public ClassMethodLoader(Project project) {
        this.project = project;
    }

    public List<Method> loadMethods(PsiFile file) {
        List<Method> methods = new ArrayList<>();

        if (file instanceof PhpFile phpFile) {
            phpFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
               @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof PhpClass phpClass) {
                        for (com.jetbrains.php.lang.psi.elements.Method actualMethod : phpClass.getOwnMethods()) {
                            Method method = extractMethodInfo(actualMethod);
                            if (method.getName() != null) {
                                methods.add(method);
                            }
                        }
                        return;
                    }
                    super.visitElement(element);
                }
            });
        }
        return methods;
    }

    public Method extractMethodInfo(com.jetbrains.php.lang.psi.elements.Method actualMethod) {
        Method method = new Method();
        if (actualMethod.getAccess().isPublic()) {
            method.setName(actualMethod.getName());
            method.setSee(
                    Objects.requireNonNull(actualMethod.getContainingClass()).getFQN()
            );
            this.getMethodInfo(method, actualMethod);
        }
        return method;
    }

    public void getMethodInfo(Method method, com.jetbrains.php.lang.psi.elements.Method actualMethod) {
        method.setReturnType(
            actualMethod.getType().global(this.project).toString()
        );

        for (Parameter param : actualMethod.getParameters()) {
            method.addParameter(param.getText());
        }
    }
}

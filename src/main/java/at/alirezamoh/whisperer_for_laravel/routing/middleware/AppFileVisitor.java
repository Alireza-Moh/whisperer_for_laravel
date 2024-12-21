package at.alirezamoh.whisperer_for_laravel.routing.middleware;

import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.ClassUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AppFileVisitor extends PsiRecursiveElementWalkingVisitor {
    private List<PsiElement> elements = new ArrayList<>();

    @Override
    public void visitElement(@NotNull PsiElement element) {
        getProjectGroupMiddlewares(element);

        getProjectMiddlewareAliases(element);

        getLaravelDefaultMiddlewares(element);

        super.visitElement(element);
    }

    private void getProjectGroupMiddlewares(@NotNull PsiElement element) {
        if (element instanceof MethodReference methodReference && Objects.equals(methodReference.getName(), "appendToGroup") && ClassUtils.isCorrectRelatedClass(methodReference, element.getProject(), "\\Illuminate\\Foundation\\Configuration\\Middleware")) {
            PsiElement parameter = methodReference.getParameter(0);

            if (parameter instanceof StringLiteralExpression) {
                elements.add(parameter);
            }
        }
    }

    private void getProjectMiddlewareAliases(@NotNull PsiElement element) {
        if (element instanceof MethodReference methodReference && Objects.equals(methodReference.getName(), "alias") && ClassUtils.isCorrectRelatedClass(methodReference, element.getProject(), "\\Illuminate\\Foundation\\Configuration\\Middleware")) {
            PsiElement parameter = methodReference.getParameter(0);

            if (parameter instanceof ArrayCreationExpression arrayCreationExpression) {
                for (PsiElement psiElement : arrayCreationExpression.getChildren()) {
                    if (psiElement instanceof ArrayHashElement arrayHashElement) {
                        elements.add(arrayHashElement.getKey());
                    }
                }
            }
        }
    }

    private void getLaravelDefaultMiddlewares(@NotNull PsiElement element) {
        if (element instanceof Method method && Objects.equals(method.getName(), "defaultAliases")) {
            for (PsiElement child : method.getChildren()) {
                if (child instanceof GroupStatement groupStatement) {
                    for (PsiElement child2 : groupStatement.getChildren()) {
                        if (child2 instanceof Statement statement) {
                            PsiElement firstChild = statement.getFirstChild();
                            if (firstChild instanceof AssignmentExpression assignmentExpression) {
                                PsiElement value = assignmentExpression.getValue();
                                if (value instanceof ArrayCreationExpression arrayCreationExpression) {
                                    for (PsiElement arrayChild : arrayCreationExpression.getChildren()) {
                                        if (arrayChild instanceof ArrayHashElement arrayHashElement) {
                                            elements.add(arrayHashElement.getKey());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public List<PsiElement> getElements() {
        return elements;
    }
}

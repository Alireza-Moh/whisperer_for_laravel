package at.alirezamoh.idea_whisperer_for_laravel.eloquent.relation.utils;

import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.LaravelPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.MethodUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RelationResolver {
    private RelationResolver() {}

    public static @NotNull List<LookupElementBuilder> getVariants(PsiElement psiElement, Project project) {
        PhpClass eloquentModel = MethodUtils.getEloquentModel(psiElement, project);
        String targetRelation = StrUtil.removeQuotes(psiElement.getOriginalElement().getText());
        List<LookupElementBuilder> variants = new ArrayList<>();

        if (eloquentModel != null) {
            if (targetRelation.contains(".")) {
                String[] parts = targetRelation.split("\\.");

                PhpClass currentModel = eloquentModel;
                for (String part : parts) {
                    if (currentModel == null) {
                        break;
                    }

                    Method foundedMethod = resolveAllRelations(currentModel, project).stream()
                        .filter(method -> method.getName().equals(part))
                        .findFirst()
                        .orElse(null);

                    if (foundedMethod == null) {
                        currentModel = null;
                    } else {
                        currentModel = findRelatedModelFromMethod(foundedMethod);
                    }
                }

                if (currentModel != null) {
                    for (Method method : resolveAllRelations(currentModel, project)) {
                        variants.add(PsiUtil.buildSimpleLookupElement(method.getName()));
                    }
                }
            }
            else {
                for (Method method : resolveAllRelations(eloquentModel, project)) {
                    variants.add(PsiUtil.buildSimpleLookupElement(method.getName()));
                }
            }
        }

        return variants;
    }

    public static List<Method> resolveAllRelations(PhpClass model, Project project) {
        List<Method> relations = new ArrayList<>();

        for (Method method : model.getOwnMethods()) {
            if (isRelationshipMethod(method, project)) {
                relations.add(method);
            }
        }

        return relations;
    }

    public static @Nullable PhpClass findRelatedModelFromMethod(Method foundedMethod) {
        return Arrays.stream(foundedMethod.getChildren())
            .filter(element -> element instanceof GroupStatementImpl)
            .flatMap(groupStatement -> Arrays.stream(groupStatement.getChildren()))
            .filter(child -> child instanceof PhpReturnImpl)
            .flatMap(phpReturn -> Arrays.stream(phpReturn.getChildren()))
            .filter(child2 -> child2 instanceof MethodReferenceImpl)
            .map(child2 -> (MethodReferenceImpl) child2)
            .map(methodReference -> methodReference.getParameter(0))
            .filter(parameter -> parameter instanceof ClassConstantReferenceImpl)
            .map(parameter -> ((ClassConstantReferenceImpl) parameter).getClassReference())
            .filter(phpExpression -> phpExpression instanceof ClassReferenceImpl)
            .map(phpExpression -> ((ClassReferenceImpl) phpExpression).resolve())
            .filter(resolvedClass -> resolvedClass instanceof PhpClass)
            .map(resolvedClass -> (PhpClass) resolvedClass)
            .findFirst()
            .orElse(null);
    }

    private static boolean isRelationshipMethod(Method method, Project project) {
        return Arrays.stream(method.getChildren())
            .filter(element -> element instanceof GroupStatementImpl)
            .flatMap(element -> Arrays.stream(element.getChildren()))
            .filter(child -> child instanceof PhpReturnImpl)
            .flatMap(child -> Arrays.stream(child.getChildren()))
            .filter(child2 -> child2 instanceof MethodReferenceImpl)
            .map(child2 -> (MethodReferenceImpl) child2)
            .anyMatch(methodReference -> {
                List<PhpClassImpl> classes = MethodUtils.resolveMethodClasses(methodReference, project);
                PhpClass relationClass = ClassUtils.getClassByFQN(project, LaravelPaths.LaravelClasses.Model);

                return relationClass != null && classes.stream()
                    .anyMatch(clazz -> ClassUtils.isChildOf(clazz, relationClass));
            });
    }
}

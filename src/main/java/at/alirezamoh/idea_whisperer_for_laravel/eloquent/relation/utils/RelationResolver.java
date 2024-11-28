package at.alirezamoh.idea_whisperer_for_laravel.eloquent.relation.utils;

import at.alirezamoh.idea_whisperer_for_laravel.eloquent.utls.EloquentUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.LaravelPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocMethod;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocMethodTagImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.*;
import kotlin.reflect.jvm.internal.impl.types.model.ArgumentList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RelationResolver {
    public RelationResolver() {}

    public static List<String> RELATION_FQN = new ArrayList<String>() {{
        add("Illuminate\\Database\\Eloquent\\Relations\\BelongsTo");
        add("Illuminate\\Database\\Eloquent\\Relations\\BelongsToMany");
        add("Illuminate\\Database\\Eloquent\\Relations\\HasMany");
        add("Illuminate\\Database\\Eloquent\\Relations\\HasManyThrough");
        add("Illuminate\\Database\\Eloquent\\Relations\\HasOne");
        add("Illuminate\\Database\\Eloquent\\Relations\\HasOneThrough");
        add("Illuminate\\Database\\Eloquent\\Relations\\MorphMany");
        add("Illuminate\\Database\\Eloquent\\Relations\\MorphOne");
        add("Illuminate\\Database\\Eloquent\\Relations\\MorphTo");
        add("Illuminate\\Database\\Eloquent\\Relations\\MorphToMany");
        add("Illuminate\\Database\\Eloquent\\Relations\\MorphPivot");
    }};

    public @NotNull List<LookupElementBuilder> getVariants(PsiElement element, Project project) {
        if (element == null) {
            return new ArrayList<>();
        }

        FoundedEloquentModel foundedEloquentModel =  EloquentUtil.getEloquentModelStart(element);
        PhpClass eloquentModel = null;
        if (foundedEloquentModel != null) {
            eloquentModel = foundedEloquentModel.model();
        }

        String targetRelation = StrUtil.removeQuotes(element.getText());
        List<LookupElementBuilder> variants = new ArrayList<>();

        if (eloquentModel != null) {
            if (targetRelation.contains(".")) {
                String[] parts = targetRelation.split("\\.");

                PhpClass currentModel = eloquentModel;
                for (String part : parts) {
                    if (currentModel == null) {
                        break;
                    }

                    PhpDocMethod foundedMethod = extractModelRelations(project, currentModel.getName()).stream()
                        .filter(relation -> relation.getName().equals(part))
                        .findFirst()
                        .orElse(null);

                    if (foundedMethod == null) {
                        currentModel = null;
                    } else {
                        currentModel = findRelatedModelFromMethod(foundedMethod);
                    }
                }

                if (currentModel != null) {
                    for (Method relation : extractModelRelations(project, currentModel.getName())) {
                        variants.add(PsiUtil.buildSimpleLookupElement(relation.getName()));
                    }
                }
            }
            else {
                List<String> relations = getNearestRelation(element, foundedEloquentModel.element());
                for (Method relation : extractModelRelations(project, eloquentModel.getName())) {
                    variants.add(PsiUtil.buildSimpleLookupElement(relation.getName()));
                }
            }
        }

        return variants;
    }

    /**
     * Retrieves the nearest chain of relations from the current PSI element up to the given MethodReference.
     */
    private List<String> getNearestRelation(PsiElement psiElement, MethodReference methodReference) {
        List<String> relationsChain = new ArrayList<>();
        PsiElement currentElement = psiElement;

        while (currentElement != null && !currentElement.isEquivalentTo(methodReference)) {
            MethodReference currentMethodReference = null;

            if (currentElement instanceof MethodReference) {
                currentMethodReference = (MethodReference) currentElement;
            } else {
                currentMethodReference = findParentMethodReference(currentElement);
            }

            if (currentMethodReference != null && isEloquentRelationMethod(currentMethodReference.getName())) {
                // Extract the relation text from the first parameter of this method reference
                List<String> methodRelations = extractRelationNamesFromMethodReference(currentMethodReference);
                relationsChain.addAll(methodRelations);

                // Move up to the parent of the method reference for further inspection
                currentElement = currentMethodReference.getParent();
            } else {
                // If we do not find a method reference, move upwards
                currentElement = currentElement.getParent();
            }
        }

        // Since we traversed from the bottom up, reverse to get the correct order
        Collections.reverse(relationsChain);

        return relationsChain;
    }

    /**
     * Extracts the relation name(s) from a given MethodReference, which should be a known relation method with a string parameter.
     */
    private List<String> extractRelationNamesFromMethodReference(MethodReference methodReference) {
        List<String> relations = new ArrayList<>();
        PsiElement[] parameters = methodReference.getParameters();

        if (parameters.length > 0 && parameters[0] instanceof StringLiteralExpression firstParam) {
            String paramText = firstParam.getText();

            if (!paramText.isEmpty()) {
                relations.add(StrUtil.removeExtension(paramText));
            }
        }

        return relations;
    }

    /**
     * Checks if a given method is likely defining an Eloquent relation by analyzing its return type and docblocks.
     */
    private boolean isRelationMethod(Method method) {
        if (method.getDocComment() != null) {
            for (PhpDocTag tag : method.getDocComment().getTagElementsByName("return")) {
                String returnType = tag.getTagValue();
                if (returnType != null && isRelationFqn(returnType)) {
                    return true;
                }
            }
        }

        // Additional logic to analyze method body can be placed here if needed.
        return false;
    }

    /**
     * Checks if the given fully qualified name (FQN) matches any known Eloquent relation classes.
     */
    private boolean isRelationFqn(String typeName) {
        // This method can check if the given type name belongs to known Eloquent relation classes
        for (String relationFqn : RELATION_FQN) {
            if (typeName.contains(relationFqn)) {
                return true;
            }
        }
        return false;
    }


    private MethodReference findParentMethodReference(PsiElement psiElement) {
        PsiElement currentElement = psiElement;
        while (currentElement != null) {
            if (currentElement instanceof MethodReference methodRef) {
                return methodRef;
            }
            currentElement = currentElement.getParent();
        }
        return null;
    }

    private static boolean isEloquentRelationMethod(String methodName) {
        return methodName != null && LaravelPaths.QUERY_RELATION_PARAMS.containsKey(methodName);
    }

    private List<PhpDocMethod> extractModelRelations(Project project, String modelName) {
        List<PhpDocMethod> relations = new LinkedList<>();

        PsiFile modelsFile = DirectoryPsiUtil.getFileByName(
            project,
            ProjectDefaultPaths.IDEA_WHISPERER_FOR_LARAVEL_MODELS_PATH
        );

        if (modelsFile != null) {
            modelsFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof PhpClass phpClass && phpClass.getName().equals(modelName)) {
                        PhpDocComment phpDocComment = phpClass.getDocComment();

                        if (phpDocComment != null) {
                            for (PhpDocMethod phpDocMethod : phpDocComment.getMethods()) {
                                PsiElement parent = phpDocMethod.getParent();

                                if (parent instanceof PhpDocMethodTagImpl phpDocMethodTag) {
                                    for (PsiElement child : phpDocMethodTag.getChildren()) {
                                        if (child instanceof PhpPsiElement phpPsiElement) {
                                            for (PsiElement el : phpPsiElement.getChildren()) {
                                                PsiReference psiReference = el.getReference();
                                                if (psiReference != null) {
                                                    PsiElement clazz = psiReference.resolve();
                                                    if (clazz instanceof PhpClass phpClass1) {
                                                        if (RELATION_FQN.contains(phpClass1.getPresentableFQN())) {
                                                            relations.add(phpDocMethod);
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
                    super.visitElement(element);
                }
            });
        }

        return relations;
    }

    public static @Nullable PhpClass findRelatedModelFromMethod(PhpDocMethod foundedMethod) {
        PsiElement parent = foundedMethod.getParent();
        for (PsiElement child : parent.getChildren()) {
            if (child instanceof PhpPsiElement phpPsiElement) {
                for (PsiElement el : phpPsiElement.getChildren()) {
                    PsiReference psiReference = el.getReference();
                    if (psiReference != null) {
                        PsiElement clazz = psiReference.resolve();
                        if (clazz instanceof PhpClass phpClass1) {
                            if (ClassUtils.isEloquentModel(phpClass1, child.getProject())) {
                                return  phpClass1;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }
}

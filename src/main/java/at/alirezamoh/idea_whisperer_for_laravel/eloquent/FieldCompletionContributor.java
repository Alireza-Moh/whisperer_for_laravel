package at.alirezamoh.idea_whisperer_for_laravel.eloquent;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.codeGenerationHelperModels.LaravelModel;
import at.alirezamoh.idea_whisperer_for_laravel.eloquent.utls.EloquentUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.codeGeneration.MigrationManager;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.MethodUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.idea_whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class FieldCompletionContributor extends CompletionContributor {
    FieldCompletionContributor() {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL),
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE)
            ),
            new CompletionProvider<>() {

                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                    PsiElement psiElement = completionParameters.getPosition();

                    if (FrameworkUtils.isLaravelFrameworkNotInstalled(psiElement.getProject())) {
                        return;
                    }

                    boolean s = shouldComplete(psiElement);
                    if (shouldComplete(psiElement)) {
                        List<String> fields = getFields(psiElement);

                        if (fields == null) {
                            return;
                        }

                        for (String fieldName : fields) {
                            completionResultSet.addElement(PsiUtil.buildSimpleLookupElement(fieldName));
                        }
                    }
                }
            }
        );
    }

    private boolean shouldComplete(PsiElement psiElement) {
        MethodReference methodReference = MethodUtils.resolveMethodReference(psiElement, 10);

        if (methodReference != null) {
            boolean allowArray = !"whereIn".equals(methodReference.getName())
                && Objects.requireNonNull(methodReference.getName()).startsWith("where");

            return !EloquentUtil.isTableMethod(methodReference) && EloquentUtil.isFieldIn(psiElement, methodReference, allowArray);
        }

        return false;
    }

/*    private @Nullable List<String> getFields(PsiElement psiElement) {
        Project project = psiElement.getProject();
        MethodReference methodInSideClosure = MethodUtils.resolveMethodReference(psiElement, 10);
        if (MethodUtils.isInsideModelQueryClosure(methodInSideClosure, project)) {
            PhpClass model = MethodUtils.getEloquentModelFromModelClosure(methodInSideClosure, project);

            if (model != null) {
                return extractModelFields(project, model.getName());
            }
        }
        else {
            boolean isModel = ClassUtils.isEloquentModel(
                MethodUtils.resolveMethodReference(psiElement, 10),
                project
            );
            String modelName = MethodUtils.resolveModelName(psiElement, project);

            if (!isModel && modelName == null) {
                return null;
            }

            return extractModelFields(project, modelName);
        }

        return null;
    }*/

    private @Nullable List<String> getFields(PsiElement psiElement) {
        Project project = psiElement.getProject();

        boolean isModel = ClassUtils.isEloquentModel(
            MethodUtils.resolveMethodReference(psiElement, 10),
            project
        );
        String modelName = MethodUtils.resolveModelName(psiElement, project);

        if (!isModel && modelName == null) {
            return null;
        }

        return extractModelFields(project, modelName);
    }

    private List<String> extractModelFields(Project project, String modelName) {
        List<String> fields = new LinkedList<>();

        PsiFile modelsFile = DirectoryPsiUtil.getFileByName(
            project,
            ProjectDefaultPaths.IDEA_WHISPERER_FOR_LARAVEL_MODELS_PATH
        );

        if (modelsFile == null) {
            extractModelFieldsFromMigrations(project, fields);
        }
        else {
            extractModelFieldsFromGeneratedHelperCode(modelName, modelsFile, fields);
        }

        return fields;
    }

    private void extractModelFieldsFromGeneratedHelperCode(String modelName, PsiFile modelsFile, List<String> fields) {
        modelsFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (element instanceof PhpClass phpClass && phpClass.getName().equals(modelName)) {
                    Collection<Field> modelFields = phpClass.getFields();
                    for (Field field : modelFields) {
                        fields.add(field.getName());
                    }
                }
                super.visitElement(element);
            }
        });
    }

    private void extractModelFieldsFromMigrations(Project project, List<String> fields) {
        MigrationManager migrationManager = new MigrationManager(project);

        for (LaravelModel model : migrationManager.visit()) {
            for (at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables.Field field : model.getFields()) {
                fields.add(field.getName());
            }
        }
    }
}

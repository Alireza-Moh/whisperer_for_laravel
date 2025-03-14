package at.alirezamoh.whisperer_for_laravel.eloquent;

import at.alirezamoh.whisperer_for_laravel.support.caching.ListenersCacheManager;
import at.alirezamoh.whisperer_for_laravel.support.utils.EloquentUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModelRelatedFilesCollector {
    /**
     * Extracts all related files (migrations, factories, events, listeners) for the given Eloquent model
     *
     * @param project  the current project
     * @param phpClass the Eloquent model class
     * @return a list of related files
     */
    public static List<PsiFile> extractRelatedFiles(Project project, PhpClass phpClass) {
        List<PsiFile> files = new ArrayList<>();

        List<PsiFile> migrationFiles = EloquentUtils.getMigrationFilesForEloquentModel(project, EloquentUtils.getTableName(phpClass));
        if (migrationFiles != null) {
            files.addAll(migrationFiles);
        }

        EloquentUtils.collectFactoriesForModel(project, phpClass.getName(), files);

        extractEventsFromModel(phpClass, files);

        ListenersCacheManager listenersCacheManager = ListenersCacheManager.getInstance(project);
        files.addAll(listenersCacheManager.getListenersForModel(phpClass.getName()));

        return files;
    }

    /**
     * Extract events directly declared in the model (dispatchesEvents field and ObservedBy attribute)
     * @param phpClass eloquent model
     * @param files the collection to add found files to
     */
    private static void extractEventsFromModel(PhpClass phpClass, List<PsiFile> files) {
        // Get events from the "dispatchesEvents" field
        Field eventField = phpClass.findOwnFieldByName("dispatchesEvents", false);
        if (eventField != null) {
            PsiElement defaultValue = eventField.getDefaultValue();
            if (defaultValue instanceof ArrayCreationExpression arrayCreationExpression) {
                for (ArrayHashElement hashElement : arrayCreationExpression.getHashElements()) {
                    PsiFile containingFile = getContainingFileFromClassConstant(hashElement.getValue());
                    if (containingFile != null) {
                        files.add(containingFile);
                    }
                }
            }
        }

        // Get events from the "ObservedBy" attribute
        for (PhpAttribute attribute : phpClass.getAttributes()) {
            if (Objects.requireNonNull(attribute.getFQN()).endsWith("ObservedBy")) {
                PsiElement parameter = attribute.getParameter(0);
                if (parameter instanceof ArrayCreationExpression arrayCreationExpression) {
                    for (PsiElement child : arrayCreationExpression.getChildren()) {
                        if (child instanceof PhpPsiElement psiElement) {
                            PsiFile containingFile = getContainingFileFromClassConstant(psiElement.getFirstChild());
                            if (containingFile != null) {
                                files.add(containingFile);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper method to process an element expected to be a ClassConstantReference.
     * If found, it resolves the reference and adds the containing file to the list.
     *
     * @param element the PSI element to process
     */
    public static @Nullable PsiFile getContainingFileFromClassConstant(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }

        if (element instanceof ClassConstantReference classConstantReference) {
            PhpExpression reference = classConstantReference.getClassReference();
            if (reference instanceof ClassReference classReference) {
                PsiReference psiReference = classReference.getReference();
                if (psiReference != null) {
                    PsiElement resolved = psiReference.resolve();
                    if (resolved instanceof PhpClass resolvedPhpClass) {
                        return resolvedPhpClass.getContainingFile();
                    }
                }
            }
        }

        return null;
    }
}

package at.alirezamoh.whisperer_for_laravel.support.providers;

import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.ClassUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides a list of Laravel models in a project.
 * This class retrieves all PHP files from the "Models" directory and,
 * if the project is module-based, also from the "Models" directory within each module.
 * It then extracts the fully qualified namespace of each model and returns a list of these namespaces.
 */
public class ModelProvider {
    /**
     * List to store the fully qualified namespaces of the models.
     */
    private List<String> models = new ArrayList<>();

    /**
     * List of laravel models
     */
    private Collection<PsiFile> originalModels;

    /**
     * Should store the file in an array for later use
     */
    private boolean withFile;

    /**
     * The current project.
     */
    private Project project;

    /**
     * @param project The current project
     */
    public ModelProvider(Project project) {
        this.project = project;
        this.withFile = false;
    }


    /**
     * @param project  The current project
     * @param withFile should save mode files
     */
    public ModelProvider(Project project, boolean withFile) {
        this.project = project;
        this.withFile = withFile;
        this.originalModels = new ArrayList<>();
    }

    /**
     * Returns a list of fully qualified namespaces of Laravel models.
     *
     * @return The list of model namespaces.
     */
    public List<String> getModels() {
        PhpClass eloquentBaseModel = ClassUtils.getEloquentBaseModel(project);
        if (eloquentBaseModel != null) {
            PhpIndex phpIndex = PhpIndex.getInstance(project);
            processSubclasses(eloquentBaseModel.getFQN(), phpIndex);
        }
        return models;
    }

    public Collection<PsiFile> getOriginalModels() {
        getModels();
        return originalModels;
    }

    /**
     * Recursively processes subclasses and collects their namespaces.
     *
     * @param classFQN The fully qualified name of the base class.
     * @param phpIndex The PhpIndex instance for accessing class hierarchy information.
     */
    private void processSubclasses(String classFQN, PhpIndex phpIndex) {
        Collection<PhpClass> subclasses = phpIndex.getDirectSubclasses(classFQN);

        for (PhpClass subclass : subclasses) {
            if (subclass.isAbstract()) {
                processSubclasses(subclass.getFQN(), phpIndex);
            } else {
                if (withFile) {
                    originalModels.add(subclass.getContainingFile());
                }
                models.add(subclass.getPresentableFQN());
            }
        }
    }
}

package at.alirezamoh.whisperer_for_laravel.support.providers;

import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Provides a list of Laravel models in a project.
 * This class retrieves all PHP files containing eloquent models
 */
public class ModelProvider {
    private static final Logger log = LoggerFactory.getLogger(ModelProvider.class);
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
        PhpClass eloquentBaseModel = PhpClassUtils.getEloquentBaseModel(project);
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
        Set<PhpClass> visited = new HashSet<>();

        phpIndex.processAllSubclasses(classFQN, visited, subclass -> {
            if (!subclass.isAbstract() && !subclass.getFQN().equals("\\Illuminate\\Foundation\\Auth\\User")) {
                if (withFile) {
                    originalModels.add(subclass.getContainingFile());
                }
                models.add(subclass.getPresentableFQN());
            }
            return true;
        });
    }
}

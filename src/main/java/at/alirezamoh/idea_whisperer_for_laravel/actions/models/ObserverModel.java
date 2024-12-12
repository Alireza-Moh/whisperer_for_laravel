package at.alirezamoh.idea_whisperer_for_laravel.actions.models;

import at.alirezamoh.idea_whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

/**
 * Model representing a Blade component class
 */
public class ObserverModel extends BaseModel {
    private String eloquentModelPath;

    private String eloquentModelName;

    private String eloquentModelNameVariable;

    private boolean addCreatingMethod;

    private boolean addCreatedMethod;

    private boolean addUpdatingMethod;

    private boolean addUpdatedMethod;

    private boolean addSavingMethod;

    private boolean addSavedMethod;

    private boolean addDeletingMethod;

    private boolean addDeletedMethod;

    private boolean addRestoringMethod;

    private boolean addRestoredMethod;

    private boolean addRetrievedMethod;

    private boolean addForceDeletingMethod;

    private boolean addForceDeletedMethod;

    private boolean addReplicatingMethod;

    private boolean hasModel;

    /**
     * @param name                      The name of policy class
     * @param unformattedModuleFullPath The unformatted module full path
     * @param formattedModuleFullPath   The formatted module full path
     */
    public ObserverModel(
        SettingsState settingsState,
        String name,
        @Nullable String eloquentModelPath,
        String unformattedModuleFullPath,
        String formattedModuleFullPath,
        boolean addCreatingMethod,
        boolean addCreatedMethod,
        boolean addUpdatingMethod,
        boolean addUpdatedMethod,
        boolean addSavingMethod,
        boolean addSavedMethod,
        boolean addDeletingMethod,
        boolean addDeletedMethod,
        boolean addRestoringMethod,
        boolean addRestoredMethod,
        boolean addRetrievedMethod,
        boolean addForceDeletingMethod,
        boolean addForceDeletedMethod,
        boolean addReplicatingMethod
    )
    {
        super(
            settingsState,
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.OBSERVER_PATH,
            "Observer",
            ".php",
            "Observers"
        );

        if (eloquentModelPath != null && !eloquentModelPath.isEmpty()) {
            this.eloquentModelPath = eloquentModelPath.substring(1);
            this.eloquentModelName = eloquentModelPath.substring(eloquentModelPath.lastIndexOf("\\") + 1);
            this.eloquentModelNameVariable = this.getModelVariableName(this.eloquentModelName);
            this.hasModel = true;
            this.addCreatingMethod = addCreatingMethod;
            this.addCreatedMethod = addCreatedMethod;
            this.addUpdatingMethod = addUpdatingMethod;
            this.addUpdatedMethod = addUpdatedMethod;
            this.addSavingMethod = addSavingMethod;
            this.addSavedMethod = addSavedMethod;
            this.addDeletingMethod = addDeletingMethod;
            this.addDeletedMethod = addDeletedMethod;
            this.addRestoringMethod = addRestoringMethod;
            this.addRestoredMethod = addRestoredMethod;
            this.addRetrievedMethod = addRetrievedMethod;
            this.addForceDeletingMethod = addForceDeletingMethod;
            this.addForceDeletedMethod = addForceDeletedMethod;
            this.addReplicatingMethod = addReplicatingMethod;
        }
    }

    public String getEloquentModelPath() {
        return eloquentModelPath;
    }

    public String getEloquentModelName() {
        return eloquentModelName;
    }

    public String getEloquentModelNameVariable() {
        return eloquentModelNameVariable;
    }

    public boolean isAddCreatingMethod() {
        return addCreatingMethod;
    }

    public boolean isAddCreatedMethod() {
        return addCreatedMethod;
    }

    public boolean isAddUpdatingMethod() {
        return addUpdatingMethod;
    }

    public boolean isAddUpdatedMethod() {
        return addUpdatedMethod;
    }

    public boolean isAddSavingMethod() {
        return addSavingMethod;
    }

    public boolean isAddSavedMethod() {
        return addSavedMethod;
    }

    public boolean isAddDeletingMethod() {
        return addDeletingMethod;
    }

    public boolean isAddDeletedMethod() {
        return addDeletedMethod;
    }

    public boolean isAddRestoringMethod() {
        return addRestoringMethod;
    }

    public boolean isAddRestoredMethod() {
        return addRestoredMethod;
    }

    public boolean isAddRetrievedMethod() {
        return addRetrievedMethod;
    }

    public boolean isAddForceDeletingMethod() {
        return addForceDeletingMethod;
    }

    public boolean isAddForceDeletedMethod() {
        return addForceDeletedMethod;
    }

    public boolean isAddReplicatingMethod() {
        return addReplicatingMethod;
    }

    public boolean isHasModel() {
        return hasModel;
    }
}

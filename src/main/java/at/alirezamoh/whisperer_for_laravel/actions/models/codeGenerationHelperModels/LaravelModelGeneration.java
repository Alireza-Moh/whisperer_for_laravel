package at.alirezamoh.whisperer_for_laravel.actions.models.codeGenerationHelperModels;

import at.alirezamoh.whisperer_for_laravel.actions.models.BaseModel;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;

import java.util.List;
import java.util.UUID;

public class LaravelModelGeneration extends BaseModel {
    private List<LaravelModel> models;

    public LaravelModelGeneration(String namespace, List<LaravelModel> laravelModels, SettingsState settingsState) {
        String shortUuid = UUID.randomUUID().toString().split("-")[0];

        this.namespace = namespace;
        this.models = laravelModels;
        this.slug = "";
        this.name = "whisperer_for_laravel_models_" + shortUuid;
        this.extension = ".php";
        this.destination = "/" + ProjectDefaultPaths.WHISPERER_FOR_LARAVEL_DIR_PATH;

        if (settingsState.isProjectDirectoryEmpty()) {
            this.filePath = ProjectDefaultPaths.WHISPERER_FOR_LARAVEL_DIR_PATH + this.name + ".php";
        }
        else {
            this.filePath = ProjectDefaultPaths.WHISPERER_FOR_LARAVEL_DIR_PATH + settingsState.getProjectDirectoryPath() + "/" + this.name + ".php";
        }
    }

    public List<LaravelModel> getModels() {
        return models;
    }

    @Override
    public void setWithoutModuleSrc() {
        this.withoutModuleSrcPath = false;
    }
}

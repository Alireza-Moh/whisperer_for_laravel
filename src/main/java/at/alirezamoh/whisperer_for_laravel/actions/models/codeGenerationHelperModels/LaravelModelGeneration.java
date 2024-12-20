package at.alirezamoh.whisperer_for_laravel.actions.models.codeGenerationHelperModels;

import at.alirezamoh.whisperer_for_laravel.actions.models.BaseModel;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;

import java.util.List;
import java.util.UUID;

public class LaravelModelGeneration extends BaseModel {
    private List<LaravelModel> models;

    public LaravelModelGeneration(String namespace, List<LaravelModel> laravelModels) {
        String shortUuid = UUID.randomUUID().toString().split("-")[0];

        this.namespace = namespace;
        this.models = laravelModels;
        this.slug = "";
        this.name = "idea_whisperer_for_laravel_models_" + shortUuid;
        this.extension = ".php";
        this.destination = "/" + ProjectDefaultPaths.IDEA_WHISPERER_FOR_LARAVEL_PATH;
        this.filePath = ProjectDefaultPaths.IDEA_WHISPERER_FOR_LARAVEL_PATH + this.name + ".php";
    }

    public List<LaravelModel> getModels() {
        return models;
    }

    @Override
    public void setWithoutModuleSrc() {
        this.withoutModuleSrcPath = false;
    }
}

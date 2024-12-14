package at.alirezamoh.idea_whisperer_for_laravel.actions.models.codeGenerationHelperModels;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.BaseModel;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;

import java.util.List;

public class LaravelModelGeneration extends BaseModel {
    private List<LaravelModel> models;

    public LaravelModelGeneration(List<LaravelModel> laravelModels) {
        this.models = laravelModels;
        this.slug = "";
        this.name = "idea_whisperer_for_laravel_models";
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

package at.alirezamoh.idea_whisperer_for_laravel.actions.models.codeGenerationHelperModels;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.BaseModel;
import at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables.Method;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;

import java.util.List;

public class FacadeBuilder extends BaseModel {
    private List<Facade> facades;

    public FacadeBuilder(List<Facade> facades) {
        this.facades = facades;
        this.slug = "";
        this.name = "idea_whisperer_for_laravel_facades";
        this.extension = ".php";
        this.destination = "/" + ProjectDefaultPaths.IDEA_WHISPERER_FOR_LARAVEL_PATH;
        this.filePath = ProjectDefaultPaths.IDEA_WHISPERER_FOR_LARAVEL_PATH + this.name + ".php";
    }

    public List<Facade> getFacades() {
        return facades;
    }
}

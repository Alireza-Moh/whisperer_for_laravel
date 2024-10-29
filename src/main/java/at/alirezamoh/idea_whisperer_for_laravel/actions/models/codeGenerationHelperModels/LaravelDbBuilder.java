package at.alirezamoh.idea_whisperer_for_laravel.actions.models.codeGenerationHelperModels;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.BaseModel;
import at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables.Method;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;

import java.util.List;

public class LaravelDbBuilder extends BaseModel {
    private List<Method> methods;

    public LaravelDbBuilder(List<Method> methods) {
        this.methods = methods;
        this.slug = "";
        this.name = "idea_whisperer_for_laravel_base_db_query_builder";
        this.extension = ".php";
        this.destination = "/" + ProjectDefaultPaths.IDEA_WHISPERER_FOR_LARAVEL_PATH;
        this.filePath = ProjectDefaultPaths.IDEA_WHISPERER_FOR_LARAVEL_PATH + this.name + ".php";
    }

    public List<Method> getMethods() {
        return methods;
    }
}

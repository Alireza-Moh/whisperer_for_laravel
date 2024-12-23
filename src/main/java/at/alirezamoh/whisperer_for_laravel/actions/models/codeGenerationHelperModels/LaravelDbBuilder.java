package at.alirezamoh.whisperer_for_laravel.actions.models.codeGenerationHelperModels;

import at.alirezamoh.whisperer_for_laravel.actions.models.BaseModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Method;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;

import java.util.List;

public class LaravelDbBuilder extends BaseModel {
    private List<Method> methods;

    public LaravelDbBuilder(List<Method> methods) {
        this.methods = methods;
        this.slug = "";
        this.name = "idea_whisperer_for_laravel_base_db_query_builder";
        this.extension = ".php";
        this.destination = "/" + ProjectDefaultPaths.WHISPERER_FOR_LARAVEL_DIR_PATH;
        this.filePath = ProjectDefaultPaths.WHISPERER_FOR_LARAVEL_DIR_PATH + this.name + ".php";
    }

    public List<Method> getMethods() {
        return methods;
    }

    @Override
    public void setWithoutModuleSrc() {
        this.withoutModuleSrcPath = false;
    }
}

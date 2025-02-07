package at.alirezamoh.whisperer_for_laravel.actions.models.codeGenerationHelperModels;

import at.alirezamoh.whisperer_for_laravel.actions.models.BaseModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Method;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;

import java.util.List;

public class LaravelDbBuilder extends BaseModel {
    private List<Method> methods;

    public LaravelDbBuilder(List<Method> methods, SettingsState settingsState) {
        this.settingsState = settingsState;
        this.methods = methods;
        this.slug = "";
        this.name = "whisperer_for_laravel_base_db_query_builder";
        this.extension = ".php";
        this.destination = settingsState.getProjectDirectoryPath() + ProjectDefaultPaths.WHISPERER_FOR_LARAVEL_DIR_PATH;

        if (settingsState.isProjectDirectoryEmpty()) {
            this.filePath = ProjectDefaultPaths.WHISPERER_FOR_LARAVEL_DIR_PATH + this.name + ".php";
        }
        else {
            this.filePath = settingsState.getProjectDirectoryPath() + ProjectDefaultPaths.WHISPERER_FOR_LARAVEL_DIR_PATH + this.name + ".php";
        }
    }

    public List<Method> getMethods() {
        return methods;
    }

    @Override
    public void setWithoutModuleSrc() {
        this.withoutModuleSrcPath = false;
    }
}

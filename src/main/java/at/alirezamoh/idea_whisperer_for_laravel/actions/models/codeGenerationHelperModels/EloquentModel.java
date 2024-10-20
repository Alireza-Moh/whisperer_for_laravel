package at.alirezamoh.idea_whisperer_for_laravel.actions.models.codeGenerationHelperModels;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.BaseModel;
import at.alirezamoh.idea_whisperer_for_laravel.support.ProjectDefaultPaths;

import java.util.List;

public class EloquentModel extends BaseModel {
    private List<Field> fields;

    private List<Method> methods;

    private List<Relation> relations;

    private String tableName;

    /**
     * @param name                      The name of the eloquent model
     * @param unformattedModuleFullPath The unformatted full path to the module
     * @param formattedModuleFullPath   The formatted full path to the module
     * @param moduleSrcPath             Module src path
     */
    public EloquentModel(
        String name,
        String unformattedModuleFullPath,
        String formattedModuleFullPath,
        String moduleSrcPath,
        List<Field> fields
    ) {
        super(
            name,
            unformattedModuleFullPath,
            formattedModuleFullPath,
            ProjectDefaultPaths.ELOQUENT_MODEL_PATH,
            "",
            ".php",
            "Models",
            moduleSrcPath
        );

        this.fields = fields;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}

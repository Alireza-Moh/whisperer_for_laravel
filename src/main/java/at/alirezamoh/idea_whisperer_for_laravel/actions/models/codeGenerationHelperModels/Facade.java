package at.alirezamoh.idea_whisperer_for_laravel.actions.models.codeGenerationHelperModels;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.BaseModel;
import at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables.Method;

import java.util.List;

public class Facade extends BaseModel {
    private String facadeName;

    private String namespace;

    private List<Method> methods;

    public Facade(String facadeName, String namespace, List<Method> methods) {
        this.facadeName = facadeName;
        this.namespace = namespace;
        this.methods = methods;
    }

    public String getFacadeName() {
        return facadeName;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public String getNamespace() {
        return namespace;
    }
}

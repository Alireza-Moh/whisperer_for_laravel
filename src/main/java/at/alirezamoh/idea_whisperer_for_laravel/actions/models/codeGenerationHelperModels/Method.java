package at.alirezamoh.idea_whisperer_for_laravel.actions.models.codeGenerationHelperModels;

import java.util.ArrayList;
import java.util.List;

public class Method {
    private String name;

    private String returnType;

    private String see;

    private List<Parameter> parameters;

    public Method() {
        this.parameters = new ArrayList<>();
    }

    public Method(String name) {
        this.name = name;
        this.parameters = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getSee() {
        return see;
    }

    public void setSee(String see) {
        this.see = see;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }
}

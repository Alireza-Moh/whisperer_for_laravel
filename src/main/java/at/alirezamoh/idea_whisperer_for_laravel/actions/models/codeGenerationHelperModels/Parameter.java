package at.alirezamoh.idea_whisperer_for_laravel.actions.models.codeGenerationHelperModels;

public class Parameter {
    private String name;

    private String type;

    private String defaultValue;

    public Parameter(String name, String type, String defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        if (defaultValue.isEmpty()) {
            return type + " $" + name;
        }
        else {
            return type + " $" + name + " = " + defaultValue;
        }
    }
}

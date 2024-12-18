package at.alirezamoh.whisperer_for_laravel.actions.models.dataTables;

public class Parameter {
    private String name;

    private String type;

    private String defaultValue;

    private String text;

    public Parameter(String name, String type, String defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public Parameter() {
    }

    public Parameter(String text) {
        this.text = text;
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

    public String getText() {
        return text;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}

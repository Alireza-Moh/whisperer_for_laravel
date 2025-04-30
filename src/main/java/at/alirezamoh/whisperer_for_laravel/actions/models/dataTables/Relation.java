package at.alirezamoh.whisperer_for_laravel.actions.models.dataTables;

public class Relation {
    private String name;

    private String type;

    private boolean arrayOrCollection;

    private String returnType;

    public Relation(String name, String type, String returnType) {
        this.name = name;
        this.type = type;
        this.returnType = returnType;
        this.validateAdditionalParameterType();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getReturnType() {
        return returnType;
    }

    public boolean isArrayOrCollection() {
        return arrayOrCollection;
    }

    private void validateAdditionalParameterType() {
        this.arrayOrCollection = false;

        switch (this.type) {
            case "hasMany", "hasManyThrough", "morphToMany", "morphMany" -> this.arrayOrCollection = true;
        }
    }
}

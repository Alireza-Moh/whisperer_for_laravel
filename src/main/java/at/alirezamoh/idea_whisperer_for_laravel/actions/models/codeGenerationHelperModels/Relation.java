package at.alirezamoh.idea_whisperer_for_laravel.actions.models.codeGenerationHelperModels;

public class Relation {
    private String name;

    private String type;

    private String related;

    private String relatedModelName;

    private boolean arrayOrCollection;

    public Relation(String name, String type, String related) {
        this.name = name;
        this.type = type;
        this.related = related;
        this.relatedModelName = related.substring(related.lastIndexOf("\\") + 1);
        this.validateAdditionalParameterType();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getRelated() {
        return related;
    }

    public String getRelatedModelName() {
        return relatedModelName;
    }

    public boolean isArrayOrCollection() {
        return arrayOrCollection;
    }

    private void validateAdditionalParameterType() {
        this.arrayOrCollection = false;

        switch (this.type) {
            case "HasMany", "MorphToMany", "MorphMany" -> this.arrayOrCollection = true;
        }
    }
}

package at.alirezamoh.idea_whisperer_for_laravel.actions.models.codeGenerationHelperModels;

import at.alirezamoh.idea_whisperer_for_laravel.support.codeGeneration.PhpTypeConverter;

public class Field {
    private String type;

    private String name;

    private boolean nullable;

    public Field(String type, String name, boolean nullable) {
        this.type = type;
        this.name = name;
        this.nullable = nullable;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return this.name;
    }

    public boolean isNullable() {
        return nullable;
    }
}

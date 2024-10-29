package at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables;

import at.alirezamoh.idea_whisperer_for_laravel.support.codeGeneration.PhpTypeConverter;

public class Field {
    private String type;

    private String name;

    private boolean nullable;

    private boolean drop = false;

    public Field(String type, String name, boolean nullable) {
        this.type = type;
        this.name = name;
        this.nullable = nullable;
    }

    public Field(String type, String name, boolean nullable, boolean drop) {
        this.type = type;
        this.name = name;
        this.nullable = nullable;
        this.drop = drop;
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

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public void setDrop(boolean drop) {
        this.drop = drop;
    }

    public boolean isDrop() {
        return drop;
    }
}

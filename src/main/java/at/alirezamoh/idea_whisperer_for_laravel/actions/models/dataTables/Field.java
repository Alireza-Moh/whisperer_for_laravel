package at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables;

import at.alirezamoh.idea_whisperer_for_laravel.support.codeGeneration.PhpTypeConverter;
import org.jetbrains.annotations.Nullable;

public class Field {
    private String type;

    private String name;

    private boolean nullable;

    private boolean drop = false;

    private boolean rename = false;

    private RenameField renameField;

    public Field(String type, String name, boolean nullable) {
        this.type = type;
        this.name = name;
        this.nullable = nullable;
    }

    public Field(String type, String name, boolean nullable, boolean drop, boolean rename, @Nullable RenameField renameField) {
        this.type = type;
        this.name = name;
        this.nullable = nullable;
        this.drop = drop;
        this.rename = rename;
        this.renameField = renameField;
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

    public boolean isRename() {
        return rename;
    }

    public void setRename(boolean rename) {
        this.rename = rename;
    }

    public void setRenameField(RenameField renameField) {
        this.renameField = renameField;
    }

    public RenameField getRenameField() {
        return renameField;
    }
}


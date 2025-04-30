package at.alirezamoh.whisperer_for_laravel.actions.models.dataTables;

/**
 * Represents a php @property-read
 */
public class ReadProperty {
    private String name;

    private String type;

    public ReadProperty(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }
}


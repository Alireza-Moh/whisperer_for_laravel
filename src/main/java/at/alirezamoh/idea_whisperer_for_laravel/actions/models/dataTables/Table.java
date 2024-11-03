package at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables;

import java.util.List;

public record Table(String name, List<Field> fields) {
}

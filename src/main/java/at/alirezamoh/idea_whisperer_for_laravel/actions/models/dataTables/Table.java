package at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables;

import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl;

import java.util.List;

public record Table(String name, MethodReferenceImpl navigationElement, List<Field> fields) {
}

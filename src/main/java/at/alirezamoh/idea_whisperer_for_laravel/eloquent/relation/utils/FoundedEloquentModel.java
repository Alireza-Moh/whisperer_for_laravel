package at.alirezamoh.idea_whisperer_for_laravel.eloquent.relation.utils;

import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;

public record FoundedEloquentModel(PhpClass model, MethodReference element) {
}

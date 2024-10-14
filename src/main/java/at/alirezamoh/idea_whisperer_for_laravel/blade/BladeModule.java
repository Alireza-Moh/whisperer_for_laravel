package at.alirezamoh.idea_whisperer_for_laravel.blade;

import com.intellij.psi.PsiDirectory;

public record BladeModule(String viewNamespace, PsiDirectory bladeDir) {}

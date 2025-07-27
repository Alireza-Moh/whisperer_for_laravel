package at.alirezamoh.whisperer_for_laravel.translation.util;

import com.intellij.psi.PsiDirectory;

public record TranslationModule(PsiDirectory translationDir, String translationNamespace) {}

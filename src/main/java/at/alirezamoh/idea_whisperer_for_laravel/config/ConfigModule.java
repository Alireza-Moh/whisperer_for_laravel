package at.alirezamoh.idea_whisperer_for_laravel.config;

import com.intellij.psi.PsiDirectory;

public record ConfigModule(String fileName, String configKeyIdentifier, PsiDirectory configDir) {}

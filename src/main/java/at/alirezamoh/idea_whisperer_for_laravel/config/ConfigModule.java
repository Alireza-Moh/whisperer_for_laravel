package at.alirezamoh.idea_whisperer_for_laravel.config;

import com.intellij.psi.PsiFile;

public record ConfigModule(PsiFile configFile, String configKeyIdentifier) {}

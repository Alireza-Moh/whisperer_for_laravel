package at.alirezamoh.whisperer_for_laravel.config.util;

import com.intellij.psi.PsiFile;

public record ConfigModule(PsiFile configFile, String configKeyIdentifier) {}

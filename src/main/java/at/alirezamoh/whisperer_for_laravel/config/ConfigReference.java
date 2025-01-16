package at.alirezamoh.whisperer_for_laravel.config;

import at.alirezamoh.whisperer_for_laravel.config.util.ConfigKeyCollector;
import at.alirezamoh.whisperer_for_laravel.config.util.ConfigKeyResolver;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConfigReference extends PsiReferenceBase<PsiElement> {
    public ConfigReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);
    }

    @Override
    public @Nullable PsiElement resolve() {
        String text = StrUtils.removeQuotes(myElement.getText());

        if (text.isEmpty()) {
            return null;
        }

        Project project = myElement.getProject();
        PsiManager psiManager = PsiManager.getInstance(project);
        ConfigKeyResolver configKeyResolver = ConfigKeyResolver.INSTANCE;

        PsiElement resolvedElement = configKeyResolver.resolveInConfigFiles(text, project, psiManager);

        if (resolvedElement != null) {
            return resolvedElement;
        }

        return configKeyResolver.resolveInServiceProviders(text, project);
    }


    @Override
    public Object @NotNull [] getVariants() {
        Project project = myElement.getProject();

        List<LookupElementBuilder> variants = ConfigKeyCollector.INSTANCE.collectViews(project);

        return variants.toArray();
    }
}

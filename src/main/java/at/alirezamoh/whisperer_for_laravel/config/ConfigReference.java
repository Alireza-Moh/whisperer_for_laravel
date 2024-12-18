package at.alirezamoh.whisperer_for_laravel.config;

import at.alirezamoh.whisperer_for_laravel.config.resolvers.ConfigFileResolver;
import at.alirezamoh.whisperer_for_laravel.config.resolvers.ConfigKeyCollector;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigReference extends PsiReferenceBase<PsiElement> {
    public ConfigReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);
    }

    @Override
    public @Nullable PsiElement resolve() {
        Project project = myElement.getProject();
        ConfigFileResolver configFileResolver = new ConfigFileResolver(project, myElement);

        return configFileResolver.resolveConfigKey();
    }

    /**
     * Returns an array of variants (code completion suggestions) for the reference
     * This method collects config keys from both standard config files and module config files
     * (if applicable) and returns them as LookupElementBuilder objects
     * @return An array of LookupElementBuilder objects representing the config key variants
     */
    @Override
    public Object @NotNull [] getVariants() {
        ConfigKeyCollector configKeyCollector = new ConfigKeyCollector(myElement.getProject());

        return configKeyCollector.startSearching().getVariants().toArray();
    }
}

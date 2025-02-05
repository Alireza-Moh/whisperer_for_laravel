package at.alirezamoh.whisperer_for_laravel.config.util;

import at.alirezamoh.whisperer_for_laravel.indexes.ConfigIndex;
import at.alirezamoh.whisperer_for_laravel.indexes.ServiceProviderIndex;
import at.alirezamoh.whisperer_for_laravel.support.WhispererForLaravelIcon;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.IdFilter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigKeyCollector {
    public static ConfigKeyCollector INSTANCE = new ConfigKeyCollector();

    public List<LookupElementBuilder> collectViews(@NotNull Project project) {
        List<LookupElementBuilder> variants = new ArrayList<>();
        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();

        getKeysFromConfigIndex(project, fileBasedIndex, variants);
        getVariantsFromServiceProviderIndex(project, fileBasedIndex, variants);

        return variants;
    }

    private void getKeysFromConfigIndex(@NotNull Project project, FileBasedIndex fileBasedIndex, List<LookupElementBuilder> variants) {
        fileBasedIndex.processAllKeys(ConfigIndex.INDEX_ID, key -> {
            fileBasedIndex.processValues(
                ConfigIndex.INDEX_ID,
                key,
                null,
                (file, value) -> {
                    variants.add(
                        buildLookupElement(key,  buildKeyValue(value))
                    );

                    return true;
                },
                GlobalSearchScope.projectScope(project)
            );
            return true;
        }, project);
    }

    private void getVariantsFromServiceProviderIndex(@NotNull Project project, FileBasedIndex fileBasedIndex, List<LookupElementBuilder> variants) {
        fileBasedIndex.processAllKeys(ServiceProviderIndex.INDEX_ID, serviceProviderKey -> {
            fileBasedIndex.processValues(
                ServiceProviderIndex.INDEX_ID,
                serviceProviderKey,
                null,
                (file, serviceProvider) -> {
                    if (serviceProvider == null) return true;

                    for (Map.Entry<String, String> entry : serviceProvider.getConfigKeys().entrySet()) {
                        String value = null;

                        String[] splitValue = entry.getValue().split("\\|");

                        if (splitValue.length >= 2) {
                            value = splitValue[0];
                        }

                        variants.add(
                            buildLookupElement(entry.getKey(),  buildKeyValue(value))
                        );
                    }

                    return true;
                },
                GlobalSearchScope.projectScope(project),
                IdFilter.getProjectIdFilter(project, true)
            );

            return true;
        },
            GlobalSearchScope.projectScope(project),
            IdFilter.getProjectIdFilter(project, true)
        );
    }

    private @NotNull String buildKeyValue(String value) {
        return (value == null || value.isEmpty()) ? "" : " = " + value.trim();
    }

    private LookupElementBuilder buildLookupElement(String key, String value) {
        return LookupElementBuilder
            .create(key)
            .withLookupString(key)
            .withPresentableText(key)
            .withTailText(value, true)
            .bold()
            .withIcon(WhispererForLaravelIcon.LARAVEL_ICON);
    }
}

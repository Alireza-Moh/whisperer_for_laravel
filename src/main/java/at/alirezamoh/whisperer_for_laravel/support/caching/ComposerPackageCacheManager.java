package at.alirezamoh.whisperer_for_laravel.support.caching;

import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.google.gson.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.jetbrains.php.composer.ComposerConfigUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

/**
 * Project-level service that caches the installed Composer packages
 * for fast, constant-time lookups.
 */
@Service(Service.Level.PROJECT)
public final class ComposerPackageCacheManager {
    /**
     * All founded composer packages
     */
    private CachedValue<Map<String, String>> packages;

    public static ComposerPackageCacheManager getInstance(@NotNull Project project) {
        return project.getService(ComposerPackageCacheManager.class);
    }

    public ComposerPackageCacheManager(Project project) {
        buildCache(project);
    }

    public Map<String, String> getInstalledPackages() {
        return packages.getValue();
    }

    public boolean isPackageInstalled(@NotNull String packageName) {
        return packages.getValue().containsKey(packageName);
    }

    public @Nullable String getInstalledVersion(@NotNull String packageName) {
        return packages.getValue().get(packageName);
    }


    private void buildCache(Project project) {
        packages = CachedValuesManager.getManager(project).createCachedValue(() -> {
            PsiFile composerPsi = PluginUtils.getComposerFile(project);
            if (composerPsi == null) {
                return CachedValueProvider.Result.create(Collections.emptyMap(), PsiModificationTracker.MODIFICATION_COUNT);
            }

            VirtualFile composerJsonFile = composerPsi.getVirtualFile();
            if (composerJsonFile == null) {
                return CachedValueProvider.Result.create(Collections.emptyMap(), composerPsi);
            }

            VirtualFile lockFile = ComposerConfigUtils.findLockFile(composerJsonFile);
            PsiFile lockPsi = null;
            if (lockFile != null) {
                lockPsi = PsiManager.getInstance(project).findFile(lockFile);
            }

            Object dependency = lockPsi != null ? lockPsi : composerPsi;

            return CachedValueProvider.Result.create(
                loadPackages(composerPsi, lockFile),
                dependency
            );
        }, false);
    }

    /**
     * Loads the installed packages from the composer.json file or form the lock file
     */
    private @NotNull Map<String, String> loadPackages(PsiFile composerPsi, VirtualFile lockFile) {
        if (composerPsi == null) {
            return Collections.emptyMap();
        }
        VirtualFile composerJson = composerPsi.getVirtualFile();
        if (composerJson == null) {
            return Collections.emptyMap();
        }

        if (lockFile != null) {
            return loadPackagesFromLock(lockFile);
        } else {
            return parseJsonDependencies(composerJson);
        }
    }

    private @NotNull Map<String, String> loadPackagesFromLock(VirtualFile lockFile) {
        return ApplicationManager.getApplication().runReadAction((Computable<Map<String, String>>) () -> {
            Map<String, String> result = new HashMap<>();
            try {
                String jsonText = com.intellij.openapi.vfs.VfsUtilCore.loadText(lockFile);
                JsonObject root = JsonParser.parseString(jsonText).getAsJsonObject();

                for (String section : List.of("packages", "packages-dev")) {
                    JsonArray arr = root.getAsJsonArray(section);
                    if (arr != null) {
                        for (JsonElement el : arr) {
                            JsonObject pkg = el.getAsJsonObject();
                            String name = pkg.get("name").getAsString();
                            String version = pkg.get("version").getAsString();
                            result.put(name, version);
                        }
                    }
                }
            } catch (IOException | JsonParseException ignored) {
                return result;
            }
            return result;
        });
    }

    private @NotNull Map<String, String> parseJsonDependencies(@NotNull VirtualFile composerJson) {
        return ApplicationManager.getApplication()
            .runReadAction((Computable<Map<String, String>>) () -> {
                Map<String, String> result = new HashMap<>();
                try {
                    String text = com.intellij.openapi.vfs.VfsUtilCore.loadText(composerJson);
                    JsonObject root = JsonParser.parseString(text).getAsJsonObject();

                    for (String section : List.of("require", "require-dev")) {
                        JsonObject obj = root.getAsJsonObject(section);
                        if (obj != null) {
                            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                                result.put(entry.getKey(), entry.getValue().getAsString());
                            }
                        }
                    }
                } catch (IOException | JsonParseException e) {
                    return result;
                }
                return result;
            });
    }
}

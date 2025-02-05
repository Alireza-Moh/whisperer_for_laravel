package at.alirezamoh.whisperer_for_laravel.config.util;

import at.alirezamoh.whisperer_for_laravel.indexes.ConfigIndex;
import at.alirezamoh.whisperer_for_laravel.indexes.ServiceProviderIndex;
import at.alirezamoh.whisperer_for_laravel.indexes.dtos.ServiceProvider;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.IdFilter;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.PhpReturnImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Resolves references to keys within Laravel config files
 */
public class ConfigKeyResolver {
    public static ConfigKeyResolver INSTANCE = new ConfigKeyResolver();

    public @Nullable PsiElement resolveInConfigFiles(@NotNull String text, @NotNull Project project, @NotNull PsiManager psiManager) {
        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();

        if (!text.contains(".")) {
            return resolveConfigFile(text, project, psiManager);
        }

        return resolveMatchingConfigKey(text, project, psiManager, fileBasedIndex);
    }

    public @Nullable PsiElement resolveInServiceProviders(@NotNull String text, @NotNull Project project) {
        AtomicReference<PsiElement> foundElement = new AtomicReference<>();
        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();

        fileBasedIndex.processAllKeys(ServiceProviderIndex.INDEX_ID, key -> {
            fileBasedIndex.processValues(
                ServiceProviderIndex.INDEX_ID,
                key,
                null,
                (file, serviceProvider) -> checkServiceProviderForConfigKey(text, project, serviceProvider, foundElement),
                GlobalSearchScope.allScope(project)
            );

            return foundElement.get() == null;
        },
            GlobalSearchScope.projectScope(project),
            IdFilter.getProjectIdFilter(project, true)
        );

        return foundElement.get();
    }

    private @Nullable PsiElement findConfigKeyInFile(@NotNull PsiFile psiFile, @NotNull String key) {
        String[] keyParts = key.split("\\.");
        PhpReturnImpl returnStatement = PsiTreeUtil.findChildOfType(psiFile, PhpReturnImpl.class);
        if (returnStatement == null) {
            return null;
        }

        ArrayCreationExpression arrayCreation = PsiTreeUtil.findChildOfType(returnStatement, ArrayCreationExpression.class);
        if (arrayCreation == null) {
            return null;
        }

        PsiElement currentElement = arrayCreation;
        for (int i = 0; i < keyParts.length; i++) {
            String currentKey = keyParts[i].trim();
            if (currentKey.isEmpty()) {
                return null;
            }

            ArrayHashElement hashElement = findArrayHashElement(currentElement, currentKey);
            if (hashElement == null) {
                return null;
            }

            if (i == keyParts.length - 1) {
                return hashElement.getValue();
            } else {
                currentElement = hashElement.getValue();
                if (!(currentElement instanceof ArrayCreationExpression)) {
                    return null;
                }
            }
        }

        return null;
    }

    private @Nullable ArrayHashElement findArrayHashElement(@NotNull PsiElement parent, @NotNull String key) {
        for (ArrayHashElement element : PsiTreeUtil.findChildrenOfType(parent, ArrayHashElement.class)) {
            PhpPsiElement keyElement = element.getKey();
            if (keyElement instanceof StringLiteralExpression stringLiteral) {
                if (stringLiteral.getContents().trim().equals(key)) {
                    return element;
                }
            }
        }
        return null;
    }

    private @Nullable PsiElement resolveConfigFile(@NotNull String text, @NotNull Project project, @NotNull PsiManager psiManager) {
        Collection<VirtualFile> configFiles = FileBasedIndex.getInstance().getContainingFiles(
            ConfigIndex.INDEX_ID,
            text,
            GlobalSearchScope.allScope(project)
        );

        for (VirtualFile configFile : configFiles) {
            String fileName = configFile.getName().replace(".php", "");
            if (fileName.equals(text)) {
                return psiManager.findFile(configFile);
            }
        }
        return null;
    }

    private @Nullable PsiElement resolveMatchingConfigKey(@NotNull String text, @NotNull Project project, @NotNull PsiManager psiManager, @NotNull FileBasedIndex fileBasedIndex) {
        AtomicReference<PsiElement> foundElement = new AtomicReference<>();

        fileBasedIndex.processValues(
            ConfigIndex.INDEX_ID,
            text,
            null,
            (file, value) -> {
                PsiFile psiFile = psiManager.findFile(file);
                if (psiFile != null) {
                    String relativePath = getRelativeConfigFilePath(file);
                    if (relativePath != null && relativePath.equals(text)) {
                        foundElement.set(psiFile);
                        return false;
                    }

                    String keyPath = extractKeyPath(text, file);
                    if (keyPath != null) {
                        PsiElement element = findConfigKeyInFile(psiFile, keyPath);
                        if (element != null) {
                            foundElement.set(element);
                            return false;
                        }
                    }
                }
                return true;
            },
            GlobalSearchScope.projectScope(project)
        );

        return foundElement.get();
    }

    private @Nullable String extractKeyPath(@NotNull String fullKey, @NotNull VirtualFile configFile) {
        String relativePath = getRelativeConfigFilePath(configFile);
        if (relativePath == null) {
            return null;
        }

        String prefixWithDot = relativePath + ".";
        if (fullKey.startsWith(prefixWithDot)) {
            return fullKey.substring(prefixWithDot.length());
        } else if (fullKey.equals(relativePath)) {
            return "";
        } else {
            return null;
        }
    }

    private @Nullable String getRelativeConfigFilePath(@NotNull VirtualFile configFile) {
        String filePath = configFile.getPath().replace('\\', '/');
        int configIndex = filePath.lastIndexOf("/config/");
        if (configIndex == -1) {
            return null;
        }

        String relativePath = filePath.substring(configIndex + "/config/".length());
        if (relativePath.endsWith(".php")) {
            relativePath = relativePath.substring(0, relativePath.length() - ".php".length());
        }

        return relativePath.replace('/', '.');
    }

    private boolean checkServiceProviderForConfigKey(
        @NotNull String text,
        @NotNull Project project,
        ServiceProvider serviceProvider,
        AtomicReference<PsiElement> foundElement
    )
    {
        for (Map.Entry<String, String> entry : serviceProvider.getConfigKeys().entrySet()) {
            String filePath = null;

            String[] parts = entry.getValue().split("\\|", 2);
            if (parts.length < 2) {
                filePath = parts[0];
            }
            else {
                filePath = parts[1];
            }

            if (!text.contains(".") && entry.getKey().equals(text)) {
                VirtualFile virtualFile = PsiElementUtils.resolveFilePath(filePath);
                if (virtualFile != null) {
                    PsiFile psiFile = PsiElementUtils.resolvePsiFile(virtualFile, project);
                    foundElement.set(psiFile);
                    return false;
                }
            }

            if (entry.getKey().equals(text)) {
                VirtualFile virtualFile = PsiElementUtils.resolveFilePath(filePath);
                if (virtualFile != null) {
                    PsiFile psiFile = PsiElementUtils.resolvePsiFile(virtualFile, project);
                    if (psiFile != null) {
                        PsiElement element = findConfigKeyInFile(psiFile, text.substring(text.indexOf('.') + 1));
                        if (element != null) {
                            foundElement.set(element);
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}

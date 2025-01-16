package at.alirezamoh.whisperer_for_laravel.indexes;

import at.alirezamoh.whisperer_for_laravel.blade.viewName.BladeModule;
import at.alirezamoh.whisperer_for_laravel.blade.viewName.visitors.BladeFileCollector;
import at.alirezamoh.whisperer_for_laravel.blade.viewName.visitors.BladeModuleServiceProviderVisitor;
import at.alirezamoh.whisperer_for_laravel.config.util.ConfigModule;
import at.alirezamoh.whisperer_for_laravel.config.util.ConfigUtil;
import at.alirezamoh.whisperer_for_laravel.config.visitors.ConfigModuleServiceProviderVisitor;
import at.alirezamoh.whisperer_for_laravel.indexes.dataExternalizeres.ServiceProviderDataExternalizer;
import at.alirezamoh.whisperer_for_laravel.indexes.dtos.ServiceProvider;
import at.alirezamoh.whisperer_for_laravel.support.utils.LaravelPaths;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ServiceProviderIndex extends FileBasedIndexExtension<String, ServiceProvider> {
    public static final ID<String, ServiceProvider> INDEX_ID = ID.create("whisperer_for_laravel.service_providers");

    @Override
    public @NotNull ID<String, ServiceProvider> getName() {
        return INDEX_ID;
    }

    @Override
    public @NotNull DataIndexer<String, ServiceProvider, FileContent> getIndexer() {
        return inputData -> {
            Project project = inputData.getProject();

            if (!PluginUtils.isLaravelProject(project) && PluginUtils.isLaravelFrameworkNotInstalled(project)) {
                return Collections.emptyMap();
            }

            PsiFile file = inputData.getPsiFile();

            if (!(file instanceof PhpFile)) {
                return Collections.emptyMap();
            }

            Map<String, ServiceProvider> result = new HashMap<>();

            for (PhpClass phpClass : PsiTreeUtil.findChildrenOfType(file, PhpClass.class)) {
                if (shouldScanFile(phpClass)) {
                    ServiceProvider serviceProvider = new ServiceProvider(
                        collectConfigKeys(phpClass, project),
                        collectBladeFiles(phpClass, project)
                    );

                    result.put(phpClass.getFQN(), serviceProvider);
                }
            }

            return result;
        };
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull DataExternalizer<ServiceProvider> getValueExternalizer() {
        return ServiceProviderDataExternalizer.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        return file -> file.getFileType() == PhpFileType.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    private boolean shouldScanFile(PhpClass phpClass) {
        ExtendsList extendsList = phpClass.getExtendsList();
        List<ClassReference> classReferences = extendsList.getReferenceElements();

        for (ClassReference classReference : classReferences) {
            if (Objects.equals(classReference.getFQN(), LaravelPaths.LaravelClasses.ServiceProvider)) {
                return true;
            }
        }

        return false;
    }

    private Map<String, String> collectConfigKeys(PhpClass serviceProvider, Project project) {
        Map<String, String> configKeys = new HashMap<>();

        ConfigModuleServiceProviderVisitor configModuleServiceProviderVisitor = new ConfigModuleServiceProviderVisitor(project);
        serviceProvider.acceptChildren(configModuleServiceProviderVisitor);

        List<ConfigModule> configModules = configModuleServiceProviderVisitor.getConfigFilesInModule();

        if (configModules.isEmpty()) {
            return configKeys;
        }

        for (ConfigModule configModule : configModules) {
            if (!(configModule.configFile() instanceof PhpFile)) {
                return configKeys;
            }

            Map<String, String> variants = new HashMap<>();
            VirtualFile configFile = configModule.configFile().getVirtualFile();
            String dottedPath = ConfigUtil.buildParentPathForConfigKey(
                configFile,
                project,
                true,
                configModule.configKeyIdentifier()
            );
            if (!dottedPath.isEmpty()) {
                ConfigUtil.iterateOverFileChildren(dottedPath, configModule.configFile(), variants);

                for (Map.Entry<String, String> entry : variants.entrySet()) {
                    configKeys.put(entry.getKey(), entry.getValue() + "|" + configFile.getPath());
                }
            }
        }

        return configKeys;
    }

    private Map<String, String> collectBladeFiles(PhpClass serviceProvider, Project project) {
        BladeModuleServiceProviderVisitor bladeModuleServiceProviderVisitor = new BladeModuleServiceProviderVisitor(project);
        Map<String, String> bladeFiles = new HashMap<>();

        serviceProvider.acceptChildren(bladeModuleServiceProviderVisitor);
        List<BladeModule> bladeModules = bladeModuleServiceProviderVisitor.getBladeFilesInModule();

        if (!bladeModules.isEmpty()) {
            for (BladeModule bladeModule : bladeModules) {
                BladeFileCollector.collectBladeFiles(
                    bladeModule.bladeDir(),
                    "",
                    bladeModule.viewNamespace(),
                    bladeFiles::put
                );
            }
        }

        return bladeFiles;
    }
}

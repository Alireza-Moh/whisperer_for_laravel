package at.alirezamoh.whisperer_for_laravel.indexes;

import at.alirezamoh.whisperer_for_laravel.indexes.dataExternalizeres.ListStringDataExternalizer;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class PolicyIndex extends FileBasedIndexExtension<String, List<String>> {
    public static final ID<String, List<String>> INDEX_ID = ID.create("whisperer_for_laravel.policies");

    private final List<String> POLICY_FOLDER_PATH = new ArrayList<>(List.of(
        "/app/Policies/",
        "app/Models/Policies/"
    ));

    @Override
    public @NotNull ID<String, List<String>> getName() {
        return INDEX_ID;
    }

    @Override
    public @NotNull DataIndexer<String, List<String>, FileContent> getIndexer() {
        return inputData -> {
            Project project = inputData.getProject();

            if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
                return Collections.emptyMap();
            }

            PsiFile file = inputData.getPsiFile();

            if (!(file instanceof PhpFile phpFile)) {
                return Collections.emptyMap();
            }

            Map<String, List<String>> policies = new HashMap<>();

            for (PhpClass phpClass : PhpClassUtils.getPhpClassesFromFile(phpFile)) {
                if (!phpClass.isAbstract()) {
                    policies.put(phpClass.getFQN(), collectPolicies(phpClass));
                }
            }

            return policies;
        };
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull DataExternalizer<List<String>> getValueExternalizer() {
        return new ListStringDataExternalizer();
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        return file -> file.getFileType() == PhpFileType.INSTANCE && isPolicyClass(file);
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public boolean traceKeyHashToVirtualFileMapping() {
        return true;
    }

    private boolean isPolicyClass(VirtualFile file) {
        String filePath = file.getPath();

        filePath = filePath.replace(file.getName(), "");

        for (String path : POLICY_FOLDER_PATH) {
            if (filePath.contains(path)) {
                return true;
            }
        }

        return false;
    }

    private List<String> collectPolicies(PhpClass phpClass) {
        List<String> policies = new ArrayList<>();

        for (Method method : PhpClassUtils.getClassPublicMethods(phpClass, true)) {
            if (StrUtils.isCamelCase(method.getName())) {
                policies.add(StrUtils.snake(method.getName(), "-") + "|" + method.getName());
            }
            else {
                policies.add(method.getName() + "|" + method.getName());
            }
        }

        return policies;
    }
}

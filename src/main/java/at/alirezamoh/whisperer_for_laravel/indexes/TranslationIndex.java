package at.alirezamoh.whisperer_for_laravel.indexes;


import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.translation.util.TranslationJsonFlattener;
import at.alirezamoh.whisperer_for_laravel.translation.util.TranslationUtil;
import com.intellij.json.psi.JsonFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;

import com.jetbrains.php.lang.psi.PhpFile;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TranslationIndex extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> INDEX_ID = ID.create("whisperer_for_laravel.translations");

    @Override
    public @NotNull ID<String, String> getName() {
        return INDEX_ID;
    }

    @Override
    public @NotNull DataIndexer<String, String, FileContent> getIndexer() {
        return inputData -> {
            Project project = inputData.getProject();

            if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
                return Collections.emptyMap();
            }

            PsiFile file = inputData.getPsiFile();
            if (file instanceof JsonFile jsonFile) {
                Map<String, String> variants = TranslationJsonFlattener.parseJsonTranslation(jsonFile, false, "");

                return Objects.requireNonNullElse(variants, Collections.emptyMap());
            }

            if (!(file instanceof PhpFile)) {
                return Collections.emptyMap();
            }

            String dottedPath = TranslationUtil.buildParentPathForTranslationKey(
                inputData.getFile(),
                project,
                false,
                ""
            );
            if (dottedPath.isEmpty()) {
                return Collections.emptyMap();
            }

            Map<String, String> variants = new HashMap<>();
            TranslationUtil.iterateOverFileChildren(dottedPath, file, variants);

            return variants;
        };
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull DataExternalizer<String> getValueExternalizer() {
        return new DataExternalizer<>() {
            @Override
            public void save(@NotNull DataOutput dataOutput, String s) throws IOException {
                if (s == null) {
                    dataOutput.writeBoolean(false);
                } else {
                    dataOutput.writeBoolean(true);
                    dataOutput.writeUTF(s);
                }
            }

            @Override
            public String read(@NotNull DataInput dataInput) throws IOException {
                boolean isNotNull = dataInput.readBoolean();
                return isNotNull ? dataInput.readUTF() : null;
            }
        };
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        return file -> {
            Project project = ProjectUtil.guessProjectForFile(file);
            if (project == null) {
                return false;
            }

            return isTranslationFile(file);
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public boolean traceKeyHashToVirtualFileMapping() {
        return true;
    }

    private boolean isTranslationFile(VirtualFile file) {
        String filePath = file.getPath();

        return filePath.contains("/resources/lang/") || filePath.contains("/lang/");
    }
}

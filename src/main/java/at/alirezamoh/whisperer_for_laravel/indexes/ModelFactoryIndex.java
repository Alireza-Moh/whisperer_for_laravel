package at.alirezamoh.whisperer_for_laravel.indexes;

import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class ModelFactoryIndex extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> INDEX_ID = ID.create("whisperer_for_laravel.model_factories");

    /**
     * Base laravel factory class
     */
    public static final String BASE_FACTORY_CLASS = "\\Illuminate\\Database\\Eloquent\\Factories\\Factory";

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

            if (!(file instanceof PhpFile)) {
                return Collections.emptyMap();
            }

            Map<String, String> factories = new HashMap<>();

            for (PhpClass phpClass : PsiTreeUtil.findChildrenOfType(file, PhpClass.class)) {
                if (shouldScanClass(phpClass)) {
                    String key = getModelNameFromClassAttribute(phpClass);
                    if (key == null) {
                        String factoryName = phpClass.getName();
                        if (factoryName.endsWith("Factory")) {
                            key = factoryName.substring(0, factoryName.length() - "Factory".length());
                        }
                    }

                    if (key != null) {
                        factories.put(key, phpClass.getFQN());
                    }
                }
            }

            return factories;
        };
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        return file -> file.getFileType() == PhpFileType.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public boolean traceKeyHashToVirtualFileMapping() {
        return true;
    }

    private boolean shouldScanClass(PhpClass phpClass) {
        ExtendsList extendsList = phpClass.getExtendsList();
        List<ClassReference> classReferences = extendsList.getReferenceElements();

        for (ClassReference classReference : classReferences) {
            if (Objects.equals(classReference.getFQN(), BASE_FACTORY_CLASS)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Extracts the model name from the specified factory class
     * The method checks for a field named "model" and, if found, extracts the class name
     * if not, it falls back to inferring the model name from the factory class name
     *
     * @param factory the factory {@code PhpClass} to inspect
     * @return the extracted model name, or {@code null} if not available
     */
    private @Nullable String getModelNameFromClassAttribute(PhpClass factory) {
        Field modelField = factory.findOwnFieldByName("model", false);

        if (modelField != null) {
            PsiElement defaultValue = modelField.getDefaultValue();
            if (defaultValue instanceof ClassConstantReference classConstantReference) {
                PhpExpression reference = classConstantReference.getClassReference();
                if (reference instanceof ClassReference classReference) {
                    return StrUtils.removeQuotes(classReference.getName());
                }
            }
        }

        return null;
    }
}

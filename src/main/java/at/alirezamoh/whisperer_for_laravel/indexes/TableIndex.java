package at.alirezamoh.whisperer_for_laravel.indexes;

import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
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
import com.intellij.util.io.VoidDataExternalizer;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class TableIndex extends FileBasedIndexExtension<String, Void> {
    public static final ID<String, Void> INDEX_ID = ID.create("whisperer_for_laravel.db_tables");

    private static final String SCHEMA_CLASS = "\\Illuminate\\Support\\Facades\\Schema";

    @Override
    public @NotNull ID<String, Void> getName() {
        return INDEX_ID;
    }

    @Override
    public @NotNull DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            Project project = inputData.getProject();

            if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
                return Collections.emptyMap();
            }

            PsiFile file = inputData.getPsiFile();

            if (!(file instanceof PhpFile)) {
                return Collections.emptyMap();
            }

            Map<String, Void> tables = new HashMap<>();

            for (MethodReference methodReference : PsiTreeUtil.findChildrenOfType(file, MethodReference.class)) {
                if (shouldScanMethod(methodReference)) {
                    String tableName = extractTableName(methodReference);
                    if (tableName != null && !tableName.isEmpty()) {
                        tables.put(tableName, null);
                    }
                }
            }

            return tables;
        };
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull DataExternalizer<Void> getValueExternalizer() {
        return VoidDataExternalizer.INSTANCE;
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

    private boolean shouldScanMethod(MethodReference methodReference) {
        ClassReferenceImpl schemaClassReference = PhpClassUtils.getClassReferenceImplFromMethodRef(methodReference);

        return isInsideUpMethod(methodReference)
            && isCreateOrTable(methodReference)
            && schemaClassReference != null
            && Objects.equals(schemaClassReference.getFQN(), SCHEMA_CLASS);
    }

    private boolean isInsideUpMethod(MethodReference methodReference) {
        Method parentMethod = PsiTreeUtil.getParentOfType(methodReference, Method.class);
        return parentMethod != null && "up".equals(parentMethod.getName());
    }

    private boolean isCreateOrTable(MethodReference methodReference) {
        String methodName = methodReference.getName();
        return "create".equals(methodName) || "table".equals(methodName);
    }

    private @Nullable String extractTableName(MethodReference methodReference) {
        PsiElement parameterTableName = methodReference.getParameter(0);

        if (parameterTableName instanceof StringLiteralExpression) {
             return StrUtils.removeQuotes(parameterTableName.getText());
        }

        return null;
    }
}

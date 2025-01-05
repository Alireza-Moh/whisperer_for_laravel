package at.alirezamoh.whisperer_for_laravel.support.eloquentUtil;

import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.whisperer_for_laravel.settings.SettingsState;
import at.alirezamoh.whisperer_for_laravel.support.ProjectDefaultPaths;
import at.alirezamoh.whisperer_for_laravel.support.directoryUtil.DirectoryPsiUtil;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EloquentUtil {
    private final List<String> PHP_DATA_TYPES = Arrays.asList(
        "int",
        "float",
        "string",
        "bool",
        "null",
        "Carbon",
        "double"
    );

    private final Project project;

    private final SettingsState settingsState;

    public EloquentUtil(Project project) {
        this.project = project;
        this.settingsState = SettingsState.getInstance(project);
    }

    public List<Field> getFields(String eloquentModelName, boolean withRelations) {
        List<Field> fields = new ArrayList<>();

        PsiDirectory pluginVendor = getPluginVendor();

        if (pluginVendor == null) {
            return fields;
        }

        for (PsiFile file : pluginVendor.getFiles()) {
            file.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof PhpClass phpClass && phpClass.getName().equals(eloquentModelName)) {
                        extractFieldsFromClass(phpClass, withRelations, fields);
                    }
                    super.visitElement(element);
                }
            });
        }

        return fields;
    }

    public @Nullable PsiDirectory getPluginVendor() {
        String path = ProjectDefaultPaths.WHISPERER_FOR_LARAVEL_DIR_PATH;

        if (!settingsState.isLaravelDirectoryEmpty()) {
            path = StrUtils.addSlashes(settingsState.getLaravelDirectoryPath(), false, true) + path;
        }

        return DirectoryPsiUtil.getDirectory(project, path);
    }

    public SettingsState getSettingsState() {
        return settingsState;
    }

    private void extractFieldsFromClass(PhpClass phpClass, boolean withRelations, List<Field> fields) {
        for (com.jetbrains.php.lang.psi.elements.Field field : phpClass.getOwnFields()) {
            PsiElement prev = field.getPrevPsiSibling();

            if (withRelations) {
                fields.add(new Field(field.getName()));
            }
            else {
                if (prev instanceof PhpDocTypeImpl phpDocType && PHP_DATA_TYPES.contains(phpDocType.getName())) {
                    fields.add(new Field(field.getName()));
                }
            }
        }
    }
}

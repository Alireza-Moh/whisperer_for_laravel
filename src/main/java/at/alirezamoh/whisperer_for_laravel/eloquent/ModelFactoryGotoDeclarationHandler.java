package at.alirezamoh.whisperer_for_laravel.eloquent;

import at.alirezamoh.whisperer_for_laravel.support.utils.EloquentUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModelFactoryGotoDeclarationHandler implements GotoDeclarationHandler {
    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement psiElement, int i, Editor editor) {
        if (psiElement == null) {
            return null;
        }

        Project project = psiElement.getProject();
        if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
            return null;
        }

        if (!(psiElement.getParent() instanceof MethodReference methodReference)) {
            return null;
        }

        if (!Objects.equals(methodReference.getName(), "factory")) {
            return null;
        }

        PhpClass model = PhpClassUtils.getPhpClassFromMethodRef(methodReference);
        if (model == null) {
            return null;
        }

        if (EloquentUtils.isEloquentModel(model, project)) {
            List<PsiFile> files = new ArrayList<>();
            EloquentUtils.collectFactoriesForModel(project, model.getName(), files);
            return files.toArray(new PsiElement[0]);
        }

        return new PsiElement[0];
    }
}

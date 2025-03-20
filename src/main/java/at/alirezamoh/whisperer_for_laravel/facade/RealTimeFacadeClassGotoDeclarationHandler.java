package at.alirezamoh.whisperer_for_laravel.facade;

import at.alirezamoh.whisperer_for_laravel.facade.util.RealTimeFacadeUtil;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles navigation to the declaration of real time facade class
 */
public class RealTimeFacadeClassGotoDeclarationHandler implements GotoDeclarationHandler {
    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor) {
        if (sourceElement == null) {
            return null;
        }

        Project project = sourceElement.getProject();
        if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
            return null;
        }

        ClassReference classReference = PsiTreeUtil.getParentOfType(sourceElement, ClassReference.class);
        if (classReference == null) {
            return null;
        }


        PhpClass facade = RealTimeFacadeUtil.getFacadeClass(project, classReference.getFQN());

        if (facade == null) {
            return null;
        }

        return new PhpClass[]{facade};
    }

    @Override
    public @Nullable String getActionText(@NotNull DataContext context) {
        return null;
    }
}
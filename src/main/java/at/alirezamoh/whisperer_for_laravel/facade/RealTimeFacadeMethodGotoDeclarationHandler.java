package at.alirezamoh.whisperer_for_laravel.facade;

import at.alirezamoh.whisperer_for_laravel.facade.util.RealTimeFacadeUtil;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles navigation to the declaration of real time facade methods
 */
public class RealTimeFacadeMethodGotoDeclarationHandler implements GotoDeclarationHandler {
    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor) {
        if (sourceElement == null) {
            return null;
        }

        Project project = sourceElement.getProject();
        if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
            return null;
        }

        PsiElement parentElement = sourceElement.getParent();
        if (!(parentElement instanceof MethodReference methodReference)) {
            return null;
        }

        PhpExpression phpExpression = methodReference.getClassReference();
        if (!(phpExpression instanceof ClassReference classReference)) {
            return null;
        }

        Method method = getFacadeMethodByName(project, classReference.getFQN(), methodReference.getName());

        if (method == null) {
            return null;
        }

        return new Method[]{method};
    }

    @Override
    public @Nullable String getActionText(@NotNull DataContext context) {
        return null;
    }

    /**
     * Retrieves a facade method by its name from the given facade class
     *
     * @param project    the current project
     * @param facadeFqn  the fully qualified name of the facade
     * @param methodName the name of the method to find
     * @return the {@link Method} matching the given name, or null if not found
     */
    private @Nullable Method getFacadeMethodByName(Project project, String facadeFqn, String methodName) {
        PhpClass facade = RealTimeFacadeUtil.getFacadeClass(project, facadeFqn);

        if (facade == null) {
            return null;
        }

        return facade.findMethodByName(methodName);
    }
}
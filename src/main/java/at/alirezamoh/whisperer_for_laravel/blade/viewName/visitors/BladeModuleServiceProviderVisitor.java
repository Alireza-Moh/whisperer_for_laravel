package at.alirezamoh.whisperer_for_laravel.blade.viewName.visitors;

import at.alirezamoh.whisperer_for_laravel.blade.viewName.BladeModule;
import at.alirezamoh.whisperer_for_laravel.support.applicationModules.visitors.BaseServiceProviderVisitor;
import at.alirezamoh.whisperer_for_laravel.support.psiUtil.PsiUtil;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.ConcatenationExpressionImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Visits a laravel service provider in a module-based Laravel application
 */
public class BladeModuleServiceProviderVisitor extends BaseServiceProviderVisitor {
    /**
     * The name of the method to look for in the service provider
     */
    private final String LOAD_VIEWS_METHOD = "loadViewsFrom";

    /**
     * A map of Blade files and their corresponding names
     */
    private List<BladeModule> bladeFilesInModule = new ArrayList<>();

    /**
     * @param project The current project
     */
    public BladeModuleServiceProviderVisitor(Project project) {
        super(project);
    }

    /**
     * Visits an element in the laravel service provider
     * @param element The PSI element being visited
     */
    @Override
    public void visitElement(@NotNull PsiElement element) {
        if (element instanceof MethodReference methodReference) {
            String methodName = methodReference.getName();
            if (methodName != null && methodName.equals(LOAD_VIEWS_METHOD)) {
                this.initParameters(methodReference);
            }
        }
        super.visitElement(element);
    }

    /**
     * Returns a map of Blade files and their corresponding names
     * @return Blade files
     */
    public List<BladeModule> getBladeFilesInModule() {
        return bladeFilesInModule;
    }

    /**
     * Iterates over the modules in the project and searches for Blade files
     * @param method The method reference being visited
     */
    private void initParameters(MethodReference method) {
        String viewNamespace = PsiUtil.getSecondParameterFromMethod(method);
        ParameterList parameters = method.getParameterList();

        if (parameters != null) {
            PsiElement namespaceParameter = parameters.getParameter(0);
            if (namespaceParameter instanceof ConcatenationExpressionImpl concatenationExpression) {
                PsiFile containingFile = concatenationExpression.getContainingFile();
                VirtualFile virtualFile = containingFile.getVirtualFile();

                if (virtualFile != null) {
                    VirtualFile parentDir = virtualFile.getParent();
                    PsiElement rightOperand = concatenationExpression.getRightOperand();
                    if (rightOperand instanceof StringLiteralExpression relativePathViewDirPath && parentDir != null) {

                        VirtualFile resolvedVirtualFile = parentDir.findFileByRelativePath(
                            StrUtils.removeQuotes(relativePathViewDirPath.getText())
                        );

                        if (resolvedVirtualFile != null && resolvedVirtualFile.isDirectory()) {
                            PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(resolvedVirtualFile);
                            if (psiDirectory != null) {
                                bladeFilesInModule.add(new BladeModule(viewNamespace, psiDirectory));
                            }
                        }
                    }
                }
            }
        }
    }
}

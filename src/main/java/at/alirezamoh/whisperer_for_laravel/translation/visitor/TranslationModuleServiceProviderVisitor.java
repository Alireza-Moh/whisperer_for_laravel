package at.alirezamoh.whisperer_for_laravel.translation.visitor;

import at.alirezamoh.whisperer_for_laravel.support.applicationModules.visitors.BaseServiceProviderVisitor;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import at.alirezamoh.whisperer_for_laravel.translation.util.TranslationModule;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.ConcatenationExpressionImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Visits module service provider to collect config keys in a Laravel project
 */
public class TranslationModuleServiceProviderVisitor extends BaseServiceProviderVisitor {
    /**
     * The name of the method to look for in the service provider
     */
    private final String TARGET_TRANSLATION_METHOD = "loadTranslationsFrom";

    private List<TranslationModule> translationFilesInModule = new ArrayList<>();

    /**
     * @param project The current project
     */
    public TranslationModuleServiceProviderVisitor(Project project) {
        super(project);
    }

    /**
     * Visits an element in the service provider
     * This method specifically checks for MethodReference elements that call the
     * 'mergeConfigFrom' method and then iterates over the modules to collect config keys
     *
     * @param element The PSI element being visited
     */
    @Override
    public void visitElement(@NotNull PsiElement element) {
        if (element instanceof MethodReference methodReference) {
            String methodName = methodReference.getName();
            if (methodName != null && methodName.equals(TARGET_TRANSLATION_METHOD)) {
                initParameters(methodReference);
            }
        }
        super.visitElement(element);
    }

    public List<TranslationModule> getTranslationFilesInModule() {
        return translationFilesInModule;
    }

    /**
     * Searches for the method parameters
     * @param method method reference being visited
     */
    private void initParameters(MethodReference method) {
        String translationNamespace = PsiElementUtils.getMethodParameterAt(method, 1);
        if (translationNamespace == null) {
            return;
        }

        ParameterList parameters = method.getParameterList();
        if (parameters == null) {
            return;
        }

        PsiElement namespaceParameter = parameters.getParameter(0);
        if (!(namespaceParameter instanceof ConcatenationExpressionImpl concatenationExpression)) {
            return;
        }

        PsiFile containingFile = concatenationExpression.getContainingFile();
        VirtualFile virtualFile = containingFile.getVirtualFile();
        if (virtualFile == null) {
            return;
        }

        VirtualFile parentDir = virtualFile.getParent();
        PsiElement rightOperand = concatenationExpression.getRightOperand();
        if (rightOperand instanceof StringLiteralExpression relativePathViewDirPath && parentDir != null) {

            VirtualFile resolvedVirtualFile = parentDir.findFileByRelativePath(
                StrUtils.removeQuotes(relativePathViewDirPath.getText())
            );

            if (resolvedVirtualFile != null && resolvedVirtualFile.isDirectory()) {
                PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(resolvedVirtualFile);
                if (psiDirectory != null) {
                    translationFilesInModule.add(new TranslationModule(psiDirectory, translationNamespace));
                }
            }
        }
    }
}

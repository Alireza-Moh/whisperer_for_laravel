package at.alirezamoh.whisperer_for_laravel.config.visitors;

import at.alirezamoh.whisperer_for_laravel.config.util.ConfigModule;
import at.alirezamoh.whisperer_for_laravel.support.applicationModules.visitors.BaseServiceProviderVisitor;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
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
public class ConfigModuleServiceProviderVisitor extends BaseServiceProviderVisitor {
    /**
     * The name of the method to look for in the service provider
     */
    private final String MERGE_CONFIG_METHOD = "mergeConfigFrom";

    private List<ConfigModule> configFilesInModule = new ArrayList<>();

    /**
     * @param project The current project
     */
    public ConfigModuleServiceProviderVisitor(Project project) {
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
            if (methodName != null && methodName.equals(MERGE_CONFIG_METHOD)) {
                initParameters(methodReference);
            }
        }
        super.visitElement(element);
    }

    public List<ConfigModule> getConfigFilesInModule() {
        return configFilesInModule;
    }

    /**
     * Searches for the method parameters
     * @param method method reference being visited
     */
    private void initParameters(MethodReference method) {
        String configKeyIdentifier = PsiElementUtils.getMethodParameterAt(method, 1);
        if (configKeyIdentifier == null) {
            return;
        }

        ParameterList parameters = method.getParameterList();
        if (parameters == null) {
            return;
        }

        PsiElement configFilePathParameter = parameters.getParameter(0);
        if (!(configFilePathParameter instanceof ConcatenationExpressionImpl concatenationExpression)) {
            return;
        }

        PsiFile containingFile = concatenationExpression.getContainingFile();
        VirtualFile virtualFile = containingFile.getVirtualFile();
        if (virtualFile == null) {
            return;
        }

        VirtualFile parentDir = virtualFile.getParent();
        PsiElement rightOperand = concatenationExpression.getRightOperand();
        if (rightOperand instanceof StringLiteralExpression relativePathConfigFilePath && parentDir != null) {
            VirtualFile resolvedVirtualFile = parentDir.findFileByRelativePath(
                StrUtils.removeQuotes(relativePathConfigFilePath.getText())
            );

            if (resolvedVirtualFile != null) {
                PsiFile configFile = PsiManager.getInstance(project).findFile(resolvedVirtualFile);
                if (configFile != null) {
                    configFilesInModule.add(
                        new ConfigModule(configFile, configKeyIdentifier)
                    );
                }
            }
        }
    }
}

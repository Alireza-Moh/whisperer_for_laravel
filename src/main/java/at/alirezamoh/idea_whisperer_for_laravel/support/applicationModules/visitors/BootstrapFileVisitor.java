package at.alirezamoh.idea_whisperer_for_laravel.support.applicationModules.visitors;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ClassConstantReference;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.impl.ArrayCreationExpressionImpl;
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpPsiElementImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpReturnImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Visits a "bootstrap/app.php" file to extract service provider files
 */
public class BootstrapFileVisitor extends PsiRecursiveElementWalkingVisitor {
    /**
     * List of discovered service provider files
     */
    private List<PsiFile> providers = new ArrayList<>();

    /**
     * Visits an element in the PSI tree
     * @param element The PSI element being visited
     */
    @Override
    public void visitElement(@NotNull PsiElement element) {
        if (element instanceof PhpReturnImpl phpReturn) {
            PsiElement[] children = phpReturn.getChildren();
            for (PsiElement child : children) {
                if (child instanceof ArrayCreationExpressionImpl arrayCreationExpression) {
                    this.searchFoServiceProviders(arrayCreationExpression);
                }
            }
        }
        super.visitElement(element);
    }

    /**
     * Searches for service providers within an array creation expression
     * @param arrayCreationExpression The array creation expression
     */
    private void searchFoServiceProviders(ArrayCreationExpressionImpl arrayCreationExpression) {
        PhpPsiElement[] providers = PsiTreeUtil.getChildrenOfType(arrayCreationExpression, PhpPsiElementImpl.class);
        if (providers != null) {
            for (PhpPsiElement provider : providers) {
                this.searchForServiceProviderFiles(provider.getFirstChild());
            }
        }
    }

    /**
     * Searches for service provider files based on a provider element
     * @param provider The provider element
     */
    private void searchForServiceProviderFiles(PsiElement provider) {
        if (provider instanceof ClassConstantReference classConstantReference) {
            PsiElement classReference = classConstantReference.getFirstChild();

            if (classReference instanceof ClassReferenceImpl classReference1) {
                PsiElement resolvedProvider = classReference1.resolve();

                if (resolvedProvider != null) {
                    this.providers.add(resolvedProvider.getContainingFile());
                }
            }
        }
    }

    /**
     * Returns the list of discovered service provider files
     * @return service provider files
     */
    public List<PsiFile> getProviders() {
        return providers;
    }
}

package at.alirezamoh.idea_whisperer_for_laravel.formRequest.visitors;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

/**
 * Visits a Form Request class to determine if a PSI element is inside the 'rules' method
 */
public class FormRequestVisitor {
    /**
     * The fully qualified name of the parent Form Request class
     */
    private final String PARENT_FORM_REQUEST_CLASS = "\\Illuminate\\Foundation\\Http\\FormRequest";

    /**
     * The name of the method that defines validation rules
     */
    private final String FORM_REQUEST_CLASS_METHOD = "rules";

    /**
     * The PSI element to check
     */
    private PsiElement psiElement;

    /**
     * Flag indicating whether the PSI element is inside the 'rules' method
     */
    private boolean isInsideCorrectMethod;

    /**
     * @param psiElement The PSI element to check
     */
    public FormRequestVisitor(PsiElement psiElement) {
        this.psiElement = psiElement;
    }

    /**
     * Checks if the PSI element is inside the 'rules' method of a Form Request class
     * This method traverses the PSI tree of the element's containing file, looking for
     * a PhpClass that extends the base Form Request class and then checks if the element
     * is inside a MethodImpl with the name 'rules'
     * @return True if the element is inside the 'rules' method, false otherwise
     */
    public boolean isInsideRuleMethod() {
        PsiFile formRequestFile = this.psiElement.getContainingFile();

        if (formRequestFile instanceof PhpFile formRequestPhpFile) {
            formRequestPhpFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof PhpClass phpClass) {
                        String parentFQN = phpClass.getSuperFQN();

                        if (parentFQN != null) {
                            if (parentFQN.equals(PARENT_FORM_REQUEST_CLASS)) {
                                isInsideMethod();
                            }
                            else {
                                Collection<PhpClass> parents = phpClass.getSuperClasses();

                                for (PhpClass parent : parents) {
                                    if (
                                        parent.getSuperFQN() != null
                                        && parent.getSuperFQN().equals(PARENT_FORM_REQUEST_CLASS)
                                    ) {
                                        isInsideMethod();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    super.visitElement(element);
                }
            });

            return this.isInsideCorrectMethod;
        }
        return false;
    }

    /**
     * Checks if the PSI element is inside a MethodImpl with the name 'rules'
     */
    private void isInsideMethod() {
        MethodImpl methodCall = PsiTreeUtil.getParentOfType(this.psiElement, MethodImpl.class);

        this.isInsideCorrectMethod = methodCall != null
            && Objects.equals(methodCall.getName(), FORM_REQUEST_CLASS_METHOD);
    }
}

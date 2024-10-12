package at.alirezamoh.idea_whisperer_for_laravel.formRequest.visitors;

import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.PhpParameterType;
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.ParameterImpl;
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Visits a method call to determine if it's a call to a specific method on a Validator or Request object
 */
public class RequestMethodVisitor {
    /**
     * The method reference being visited
     */
    private MethodReference methodReference;

    /**
     * @param element The PSI element representing the method call
     */
    public RequestMethodVisitor(PsiElement element) {
        this.methodReference = PsiUtil.getMethodReferenceImpl(element);
    }

    /**
     * Checks if the method call is to a specific method on a Validator object
     * @param methodNameToCheck The name of the method to check
     * @param classFqnToCheck   The fully qualified name of the Validator class
     * @return                  True if the method call matches the criteria, false otherwise
     */
    public boolean isInsideValidatorMethod(@NotNull String methodNameToCheck, @NotNull String classFqnToCheck) {
        if (isCorrectMethod(methodNameToCheck)) {
            PhpExpression variableImpl = this.methodReference.getClassReference();

            if (variableImpl instanceof ClassReferenceImpl) {
                PsiReference reference = variableImpl.getReference();

                if (reference != null) {
                    PsiElement potentialPhpClass = reference.resolve();

                    return potentialPhpClass instanceof PhpClass phpClass
                        && phpClass.getPresentableFQN().equals(classFqnToCheck);
                }
            }
        }

        return false;
    }

    /**
     * Checks if the method call is to a specific method on a Request object
     * @param methodNameToCheck The name of the method to check
     * @param classFqnToCheck   The fully qualified name of the Request class
     * @return                  True if the method call matches the criteria, false otherwise
     */
    public boolean isInsideRequestMethod(@NotNull String methodNameToCheck, @NotNull String classFqnToCheck) {
        if (isCorrectMethod(methodNameToCheck)) {
            PhpExpression variableImpl = this.methodReference.getClassReference();

            if (variableImpl instanceof VariableImpl) {
                PsiReference reference = variableImpl.getReference();

                if (reference != null) {
                    PsiElement potentialPhpClass = reference.resolve();

                    if (potentialPhpClass instanceof ParameterImpl parameter) {
                        PhpParameterType phpType = parameter.getTypeDeclaration();
                        if (phpType != null) {
                            PsiElement potentialRequestClass = phpType.getFirstChild();

                            return potentialRequestClass instanceof ClassReferenceImpl requestClass
                                && requestClass.getFQN() != null
                                && requestClass.getFQN().equals(classFqnToCheck);
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks if the method reference has the correct name
     * @param methodNameToCheck The name of the method to check
     * @return                  True if the method reference has the correct name, false otherwise
     */
    private boolean isCorrectMethod(@NotNull String methodNameToCheck) {
        return this.methodReference != null
            && this.methodReference.getName() != null
            && this.methodReference.getName().equals(methodNameToCheck);
    }
}

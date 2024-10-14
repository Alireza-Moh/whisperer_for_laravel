package at.alirezamoh.idea_whisperer_for_laravel.gate.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;

public class GateUtil {
    private static String GATE_NAMESPACE = "Illuminate\\Support\\Facades\\Gate";

    private static String GATE_METHOD = "define";

    public static boolean isGateFacadeMethod(MethodReference method) {
        String methodName = method.getName();
        PhpExpression classReference = method.getClassReference();

        if (classReference instanceof ClassReferenceImpl classReference1) {
            PsiReference reference = classReference1.getReference();

            if (reference != null) {
                PsiElement potentialGateClass = reference.resolve();

                return methodName != null
                    && methodName.equals(GATE_METHOD)
                    && potentialGateClass instanceof PhpClass gatePhpClass
                    && gatePhpClass.getPresentableFQN().equals(GATE_NAMESPACE);
            }
        }

        return false;
    }
}

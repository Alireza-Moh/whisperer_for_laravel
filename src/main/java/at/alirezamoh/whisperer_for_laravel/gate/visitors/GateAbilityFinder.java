package at.alirezamoh.whisperer_for_laravel.gate.visitors;

import at.alirezamoh.whisperer_for_laravel.gate.util.GateUtil;
import at.alirezamoh.whisperer_for_laravel.support.psiUtil.PsiUtil;
import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import org.jetbrains.annotations.NotNull;

public class GateAbilityFinder extends PsiRecursiveElementWalkingVisitor {
    private PsiElement myElement;

    private PsiElement foundedAbility;

    public GateAbilityFinder(PsiElement element) {
        myElement = element;
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {
        if (element instanceof MethodReference methodReference) {
            if (GateUtil.isGateFacadeMethod(methodReference, methodReference.getProject())) {
                this.getAbility(methodReference);
            }
        }
        super.visitElement(element);
    }

    public PsiElement getFoundedAbility() {
        return foundedAbility;
    }

    public void getAbility(MethodReference method) {
        String ability = PsiUtil.getFirstParameterFromMethod(method);

        if (ability != null && ability.equals(StrUtil.removeQuotes(myElement.getText()))) {
            foundedAbility = method;
        }
    }
}

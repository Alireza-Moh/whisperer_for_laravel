package at.alirezamoh.whisperer_for_laravel.gate.visitors;

import at.alirezamoh.whisperer_for_laravel.gate.util.GateUtil;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public @Nullable PsiElement getFoundedAbility() {
        return foundedAbility;
    }

    public void getAbility(MethodReference method) {
        String ability = PsiElementUtils.getMethodParameterAt(method, 0);

        if (ability != null && ability.equals(StrUtils.removeQuotes(myElement.getText()))) {
            foundedAbility = method;
        }
    }
}

package at.alirezamoh.idea_whisperer_for_laravel.gate;

import at.alirezamoh.idea_whisperer_for_laravel.gate.visitors.GateProcessor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GateReference extends PsiReferenceBase<PsiElement> {
    private GateProcessor gateProcessor;

    public GateReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);
        gateProcessor = new GateProcessor(element.getProject());
    }

    @Override
    public @Nullable PsiElement resolve() {
        return gateProcessor.findGateAbility(myElement);
    }

    @Override
    public Object @NotNull [] getVariants() {
        return gateProcessor.collectGates().toArray();
    }
}

package at.alirezamoh.whisperer_for_laravel.gate;

import at.alirezamoh.whisperer_for_laravel.gate.visitors.GateProcessor;
import at.alirezamoh.whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
        List<LookupElementBuilder> variants = new ArrayList<>();

        for (String gate : gateProcessor.collectGates()) {
            variants.add(
                PsiUtil.buildSimpleLookupElement(gate)
            );
        }

        return variants.toArray();
    }
}

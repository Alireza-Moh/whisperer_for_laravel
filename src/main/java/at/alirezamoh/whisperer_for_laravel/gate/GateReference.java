package at.alirezamoh.whisperer_for_laravel.gate;

import at.alirezamoh.whisperer_for_laravel.gate.visitors.GateProcessor;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GateReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private GateProcessor gateProcessor;

    public GateReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);
        gateProcessor = new GateProcessor(element.getProject());
    }

    @Override
    public @Nullable PsiElement resolve() {
        return null;
    }

    @Override
    public Object @NotNull [] getVariants() {
        List<LookupElementBuilder> variants = new ArrayList<>();

        for (String gate : gateProcessor.collectGates(true)) {
            variants.add(
                PsiElementUtils.buildSimpleLookupElement(gate)
            );
        }

        return variants.toArray();
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean b) {
        List<ResolveResult> results = new ArrayList<>();

        for (PsiElement element : gateProcessor.findGateAbility(myElement, true)) {
            results.add(new PsiElementResolveResult(element));
        }

        return results.toArray(new ResolveResult[0]);
    }
}

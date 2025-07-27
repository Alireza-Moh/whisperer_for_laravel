package at.alirezamoh.whisperer_for_laravel.support;

import com.jetbrains.php.completion.PhpLookupElement;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import org.jetbrains.annotations.NotNull;

public class MethodLookupElement extends PhpLookupElement {
    public MethodLookupElement(@NotNull PhpNamedElement namedElement) {
        super(namedElement);
    }

    @Override
    protected void updatePresentation(PhpNamedElement myNamedElement) {
        this.bold = true;
        this.handler = null;
        this.icon = WhispererForLaravelIcon.LARAVEL_ICON;
        this.lookupString = myNamedElement.getName();
    }
}

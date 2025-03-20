package at.alirezamoh.whisperer_for_laravel.packages.livewire.property;

import at.alirezamoh.whisperer_for_laravel.packages.livewire.LivewireUtil;
import at.alirezamoh.whisperer_for_laravel.packages.livewire.property.utils.LivewirePropertyProvider;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.jetbrains.php.blade.psi.BladePsiLanguageInjectionHost;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A GoToDeclarationHandler that enables navigation to properties defined
 * in the Livewire PHP class associated with a livewire blade file
 */
public class LivewirePropertyGotoDeclarationHandler implements GotoDeclarationHandler {

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor) {
        if (sourceElement == null) {
            return null;
        }

        Project project = sourceElement.getProject();

        if (LivewireUtil.shouldNotCompleteOrNavigate(project)) {
            return null;
        }

        PsiElement originalPosition = sourceElement.getOriginalElement();
        PsiFile originalFile = sourceElement.getContainingFile();
        if (originalPosition != null && InjectedLanguageManager.getInstance(originalFile.getProject()).isInjectedFragment(originalFile)) {
            PsiLanguageInjectionHost host = LivewireUtil.getFromPsiLanguageInjectionHost(project, originalPosition);
            if (host instanceof BladePsiLanguageInjectionHost) {
                String text = StrUtils.removeQuotes(sourceElement.getText());
                if (!text.startsWith("$")) {
                    return null;
                }

                text = text.substring(1);

                return Objects.requireNonNull(
                    LivewirePropertyProvider.resolveProperty(project, originalFile, text, false)
                ).toArray(new PsiElement[0]);
            }

            if (host instanceof StringLiteralExpression stringLiteralExpression) {

                String text = StrUtils.removeQuotes(sourceElement.getText());
                text = getValueFromInline(text);
                if (Objects.equals(stringLiteralExpression.getName(), "HTML")) {
                    return Objects.requireNonNull(
                        LivewirePropertyProvider.resolveProperty(project, host.getContainingFile(), text, false)
                    ).toArray(new PsiElement[0]);
                }
            }
        }

        return null;
    }

    /**
     * Removes the {{ }}, leading $, and any extra spaces
     * from a string of the form: {{ $variable }}
     *
     * @param input the original string
     * @return a cleaned-up string without {{ }}, $, or extra whitespace
     */
    private String getValueFromInline(String input) {
        if (input == null) {
            return null;
        }

        input = input.trim();

        if (input.startsWith("{{") && input.endsWith("}}")) {
            input = input.substring(2, input.length() - 2).trim();

            if (input.startsWith("$")) {
                input = input.substring(1).trim();
            }
        }

        return input;
    }
}
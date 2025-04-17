package at.alirezamoh.whisperer_for_laravel.translation;

import at.alirezamoh.whisperer_for_laravel.support.WhispererForLaravelIcon;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PresentableTranslationElement extends FakePsiElement {
    private final PsiElement originalElement;

    private final String filePath;

    private final String translationKey;

    public PresentableTranslationElement(@NotNull PsiElement originalElement, @NotNull String filePath, @NotNull String translationKey) {
        this.originalElement = originalElement;
        this.filePath = filePath;
        this.translationKey = translationKey;
    }

    @Override
    public PsiElement getParent() {
        return originalElement.getParent();
    }

    @Override
    public @NotNull Project getProject() {
        return super.getProject();
    }

    @Override
    public @NotNull Language getLanguage() {
        return originalElement.getLanguage();
    }

    @Override
    public @Nullable TextRange getTextRange() {
        return originalElement.getTextRange();
    }

    @Override
    public int getTextOffset() {
        return originalElement.getTextOffset();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Override
            public @NlsSafe @Nullable String getPresentableText() {
                return translationKey;
            }

            @Override
            public @NlsSafe @Nullable String getLocationString() {
                return "(" + filePath + ")";
            }

            @Override
            public @Nullable Icon getIcon(boolean b) {
                return WhispererForLaravelIcon.LARAVEL_ICON;
            }
        };
    }
}
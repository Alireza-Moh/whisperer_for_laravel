package at.alirezamoh.whisperer_for_laravel.postfixes.dd;

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class DDPostfixProvider implements PostfixTemplateProvider {
    @Override
    public @NotNull Set<PostfixTemplate> getTemplates() {
        return Set.of(new DDPostfixTemplate());
    }

    @Override
    public boolean isTerminalSymbol(char c) {
        return c == '.';
    }

    @Override
    public void preExpand(@NotNull PsiFile psiFile, @NotNull Editor editor) {

    }

    @Override
    public void afterExpand(@NotNull PsiFile psiFile, @NotNull Editor editor) {

    }

    @Override
    public @NotNull PsiFile preCheck(@NotNull PsiFile psiFile, @NotNull Editor editor, int i) {
        return psiFile;
    }
}

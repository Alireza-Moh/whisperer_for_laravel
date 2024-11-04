package at.alirezamoh.idea_whisperer_for_laravel.postfixes.dd;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.impl.StatementImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DDPostfixTemplate extends PostfixTemplate implements DumbAware {
    protected DDPostfixTemplate() {
        super("dd", "dd", "dd(User::find(1));", new DDPostfixProvider());
    }

    @Override
    public boolean isApplicable(@NotNull PsiElement psiElement, @NotNull Document document, int i) {
        StatementImpl statement = PsiTreeUtil.getParentOfType(psiElement, StatementImpl.class);
        return statement != null;
    }

    @Override
    public void expand(@NotNull PsiElement psiElement, @NotNull Editor editor) {
        Project project = psiElement.getProject();
        TemplateManager manager = TemplateManager.getInstance(project);
        Template template = manager.createTemplate("", "");
        template.setToReformat(true);

        StatementImpl statement = PsiTreeUtil.getParentOfType(psiElement, StatementImpl.class);

        if (statement != null) {
            editor.getDocument().deleteString(statement.getTextRange().getStartOffset(), statement.getTextRange().getEndOffset());

            String expressionText = statement.getText();

            if (expressionText.endsWith(";")) {
                expressionText = expressionText.substring(0, expressionText.length() - 1);
            }

            template.addTextSegment("dd(");
            template.addVariable("expression", new TextExpression(expressionText), false);
            template.addTextSegment(");");

            manager.startTemplate(editor, template);
        }
    }
}

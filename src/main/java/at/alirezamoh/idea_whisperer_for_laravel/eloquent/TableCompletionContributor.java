package at.alirezamoh.idea_whisperer_for_laravel.eloquent;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables.Table;
import at.alirezamoh.idea_whisperer_for_laravel.eloquent.utls.EloquentUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.codeGeneration.MigrationManager;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.FrameworkUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.MethodUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import org.jetbrains.annotations.NotNull;

public class TableCompletionContributor extends CompletionContributor {
    TableCompletionContributor() {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withElementType(PhpTokenTypes.tsSTRINGS),
            new CompletionProvider<>() {

                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                    PsiElement psiElement = completionParameters.getPosition();
                    Project project = psiElement.getProject();

                    if (FrameworkUtils.isLaravelFrameworkNotInstalled(psiElement.getProject())) {
                        return;
                    }

                    if (isInsideCorrectMethod(psiElement)) {
                        MigrationManager migrationManager = new MigrationManager(project);

                        for (Table table : migrationManager.getTables()) {
                            completionResultSet.addElement(PsiUtil.buildSimpleLookupElement(table.name()));
                        }
                    }
                }
            }
        );
    }

    private boolean isInsideCorrectMethod(PsiElement psiElement) {
        MethodReference methodReference = MethodUtils.resolveMethodReference(psiElement, 10);

        return methodReference != null && ClassUtils.isLaravelRelatedClass(methodReference, psiElement.getProject())
            && EloquentUtil.isTableMethod(methodReference)
            && EloquentUtil.isTableParam(methodReference, psiElement);
    }
}

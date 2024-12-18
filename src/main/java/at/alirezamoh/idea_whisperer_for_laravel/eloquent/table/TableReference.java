package at.alirezamoh.idea_whisperer_for_laravel.eloquent.table;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables.Table;
import at.alirezamoh.idea_whisperer_for_laravel.support.codeGeneration.MigrationManager;
import at.alirezamoh.idea_whisperer_for_laravel.support.psiUtil.PsiUtil;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TableReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    /**
     * The current project
     */
    private Project project;

    /**
     * @param element        The PSI element representing the route name reference
     * @param rangeInElement The text range of the reference within the element
     */
    public TableReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);

        this.project = element.getProject();
    }

    @Override
    public @Nullable PsiElement resolve() {
        return null;
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean b) {
        MigrationManager migrationManager = new MigrationManager(project);

        List<ResolveResult> resolveResults = new ArrayList<>();

        for (Table table : migrationManager.getTables()) {
            if (table.name().equals(StrUtil.removeQuotes(myElement.getText()))) {
                resolveResults.add(
                    new PsiElementResolveResult(
                        table.navigationElement()
                    )
                );
            }
        }

        return resolveResults.toArray(new ResolveResult[0]);
    }

    @Override
    public Object @NotNull [] getVariants() {
        MigrationManager migrationManager = new MigrationManager(project);

        List<LookupElementBuilder> variants = new ArrayList<>();

        for (Table table : migrationManager.getTables()) {
            variants.add(
                PsiUtil.buildSimpleLookupElement(table.name())
            );
        }

        return variants.toArray();
    }
}

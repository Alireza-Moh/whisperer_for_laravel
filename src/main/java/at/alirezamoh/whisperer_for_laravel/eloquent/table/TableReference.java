package at.alirezamoh.whisperer_for_laravel.eloquent.table;

import at.alirezamoh.whisperer_for_laravel.indexes.TableIndex;
import at.alirezamoh.whisperer_for_laravel.support.utils.EloquentUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.IdFilter;
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
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean b) {
        String tableName = StrUtils.removeQuotes(myElement.getText());

        List<PsiFile> migrations = EloquentUtils.getMigrationFilesForEloquentModel(project, tableName);

        if (migrations == null) {
            return ResolveResult.EMPTY_ARRAY;
        }

        List<ResolveResult> results = new ArrayList<>();
        for (PsiFile migration : migrations) {
            results.add(new PsiElementResolveResult(migration));
        }

        return results.toArray(new ResolveResult[0]);
    }

    @Override
    public Object @NotNull [] getVariants() {
        List<LookupElementBuilder> variants = new ArrayList<>();
        FileBasedIndex.getInstance().processAllKeys(
            TableIndex.INDEX_ID,
            key -> {
                if (key instanceof String table) {
                    variants.add(PsiElementUtils.buildSimpleLookupElement(table));
                }
                return true;
            },
            GlobalSearchScope.projectScope(project),
            IdFilter.getProjectIdFilter(project, false)
        );

        return variants.toArray();
    }
}

package at.alirezamoh.whisperer_for_laravel.eloquent.table;

import at.alirezamoh.whisperer_for_laravel.eloquent.table.indexes.TableIndex;
import at.alirezamoh.whisperer_for_laravel.support.psiUtil.PsiUtil;
import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
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
        String text = StrUtil.removeQuotes(myElement.getText());


        List <List<String>> paths = FileBasedIndex.getInstance().getValues(
            TableIndex.INDEX_ID,
            text,
            GlobalSearchScope.allScope(project)
        );

        if (paths.isEmpty()) {
            return ResolveResult.EMPTY_ARRAY;
        }

        List<String> flattenedPaths = new ArrayList<>();
        for (List<String> pathList : paths) {
            if (pathList != null) {
                flattenedPaths.addAll(pathList);
            }
        }

        List<ResolveResult> results = new ArrayList<>();
        for (String path : flattenedPaths) {
            if (path != null) {
                VirtualFile targetFile = LocalFileSystem.getInstance().findFileByPath(path);

                if (targetFile != null) {
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(targetFile);

                    if (psiFile != null) {
                        results.add(new PsiElementResolveResult(psiFile));
                    }
                }
            }
        }

        return results.toArray(new ResolveResult[0]);
    }

    @Override
    public Object @NotNull [] getVariants() {
        List<LookupElementBuilder> variants = new ArrayList<>();
        Collection<String> tables = FileBasedIndex.getInstance().getAllKeys(TableIndex.INDEX_ID, project);

        for (String table : tables) {
            variants.add(
                PsiUtil.buildSimpleLookupElement(table)
            );
        }

        return variants.toArray();
    }
}

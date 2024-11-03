package at.alirezamoh.idea_whisperer_for_laravel.eloquent.relation;

import at.alirezamoh.idea_whisperer_for_laravel.eloquent.relation.utils.RelationResolver;
import at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils.MethodUtils;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RelationReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private Project project;

    public RelationReference(@NotNull PsiElement element) {
        super(element);

        project = element.getProject();
    }

    @Override
    public @Nullable PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);

        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean b) {
        PhpClass eloquentModel = MethodUtils.getEloquentModel(myElement, project);
        String targetRelation = StrUtil.removeQuotes(myElement.getOriginalElement().getText());
        List<ResolveResult> results = new ArrayList<>();

        if (eloquentModel != null) {
            if (targetRelation.contains(".")) {
                String[] parts = targetRelation.split("\\.");

                PhpClass currentModel = eloquentModel;
                for (String part : parts) {
                    if (currentModel == null) {
                        break;
                    }

                    Method foundedMethod = RelationResolver.resolveAllRelations(currentModel, project).stream()
                        .filter(method -> method.getName().equals(part))
                        .findFirst()
                        .orElse(null);

                    if (foundedMethod == null) {
                        currentModel = null;
                    } else {
                        for (Method method : RelationResolver.resolveAllRelations(currentModel, project)) {
                            if (method.getName().equals(part)) {
                                results.add(new PsiElementResolveResult(method));
                            }
                        }
                        currentModel = RelationResolver.findRelatedModelFromMethod(foundedMethod);
                    }
                }
            }
        }

        return results.toArray(new ResolveResult[0]);
    }

    @Override
    public Object @NotNull [] getVariants() {
        for (LookupElementBuilder l : RelationResolver.getVariants(myElement.getOriginalElement(), project)) {
            System.out.println(l.getLookupString());
        }
        return RelationResolver.getVariants(myElement.getOriginalElement(), project).toArray();
    }
}

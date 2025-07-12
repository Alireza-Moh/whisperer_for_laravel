package at.alirezamoh.whisperer_for_laravel.routing.routeName;

import at.alirezamoh.whisperer_for_laravel.indexes.RouteIndex;
import at.alirezamoh.whisperer_for_laravel.routing.RouteUtils;
import at.alirezamoh.whisperer_for_laravel.support.WhispererForLaravelIcon;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.IdFilter;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This class resolves references to route names and provides code completion
 * suggestions for route names in various contexts, such as in the `route`
 */
public class RouteReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    /**
     * The current project
     */
    private Project project;

    /**
     * @param element        The PSI element representing the route name reference
     * @param rangeInElement The text range of the reference within the element
     */
    public RouteReference(@NotNull PsiElement element, TextRange rangeInElement) {
        super(element, rangeInElement);

        this.project = element.getProject();
    }

    @Override
    public @Nullable PsiElement resolve() {
        return null;
    }

    /**
     * Returns an array of route names
     * @return route names list
     */
    @Override
    public Object @NotNull [] getVariants() {
        List<LookupElementBuilder> variants = new ArrayList<>();

        FileBasedIndex.getInstance().processAllKeys(
            RouteIndex.INDEX_ID,
            route -> {
                String[] split = route.split(" \\| ");
                if (split.length >= 3) {
                    variants.add(
                        LookupElementBuilder
                            .create(split[1])
                            .bold()
                            .withTypeText(split[0], true)
                            .withIcon(WhispererForLaravelIcon.LARAVEL_ICON)
                    );
                }
                return true;
            },
            GlobalSearchScope.projectScope(project),
            IdFilter.getProjectIdFilter(project, false)
        );

        return variants.toArray();
    }

    /**
     * Resolves the route name reference to the corresponding PSI element
     * This method searches for the route name in route files and, if applicable,
     * in module service providers
     * @return The resolved route or null
     */
    @Override
    public ResolveResult @NotNull [] multiResolve(boolean b) {
        String routeName = StrUtils.removeQuotes(myElement.getText());
        List<ResolveResult> results = new ArrayList<>();
        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();

        Set<String> matchingKeys = RouteUtils.getMatchingRouteNames(routeName, project);
        for (String key : matchingKeys) {
            String[] split = key.split(" \\| ");

            if (split.length >= 3 && split[1].equals(routeName)) {
                Collection<VirtualFile> files = fileBasedIndex.getContainingFiles(
                    RouteIndex.INDEX_ID,
                    key,
                    GlobalSearchScope.projectScope(project)
                );

                for (VirtualFile file : files) {
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                    if (psiFile != null) {
                        PsiElement element = psiFile.findElementAt(Integer.parseInt(split[2]));
                        if (element != null) {
                            PsiElement parent = element.getParent().getParent();
                            if (parent instanceof MethodReference methodReference) {
                                results.add(new PsiElementResolveResult(methodReference));
                            }
                        }
                    }
                }
            }
        }

        return results.toArray(new ResolveResult[0]);
    }
}

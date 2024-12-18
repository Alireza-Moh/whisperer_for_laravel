package at.alirezamoh.whisperer_for_laravel.routing;

import at.alirezamoh.whisperer_for_laravel.routing.indexes.RouteData;
import at.alirezamoh.whisperer_for_laravel.routing.indexes.RouteIndex;
import at.alirezamoh.whisperer_for_laravel.support.WhispererForLaravelIcon;
import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides references to Laravel routes within a project
 * This class resolves references to route names and provides code completion
 * suggestions for route names in various contexts, such as in the `route`
 * helper function or in Blade templates
 */
public class RouteReference extends PsiReferenceBase<PsiElement> {
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

    /**
     * Resolves the route name reference to the corresponding PSI element
     * This method searches for the route name in route files and, if applicable,
     * in module service providers
     * @return The resolved route or null
     */
    @Override
    public @Nullable PsiElement resolve() {
        String routeName = StrUtil.removeQuotes(myElement.getText());
        AtomicReference<PsiElement> foundedElement = new AtomicReference<>();

        FileBasedIndex.getInstance().processAllKeys(RouteIndex.INDEX_ID, fileName -> {
            List<List<RouteData>> routes = FileBasedIndex.getInstance()
                .getValues(RouteIndex.INDEX_ID, fileName, GlobalSearchScope.projectScope(project));

            for (List<RouteData> allRoutes : routes) {
                for (RouteData route : allRoutes) {
                    if (route.name().equals(routeName)) {
                        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(route.filePath());

                        if (virtualFile == null) {
                            return false;
                        }

                        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                        if (psiFile != null) {
                            PsiElement element = psiFile.findElementAt(route.offset());
                            if (element != null) {
                                foundedElement.set(element);

                                break;
                            }
                        }
                    }
                }
            }
            return true;
        }, project);

        return foundedElement.get();
    }

    /**
     * Returns an array of route names
     * @return route names list
     */
    @Override
    public Object @NotNull [] getVariants() {
        List<LookupElementBuilder> variants = new ArrayList<>();

        FileBasedIndex.getInstance().processAllKeys(RouteIndex.INDEX_ID, key -> {
            List<List<RouteData>> routes = FileBasedIndex.getInstance()
                .getValues(RouteIndex.INDEX_ID, key, GlobalSearchScope.projectScope(project));

            for (List<RouteData> allRoutes : routes) {
                for (RouteData route : allRoutes) {
                    variants.add(
                        LookupElementBuilder
                            .create(route.name())
                            .bold()
                            .withTypeText(route.uri(), true)
                            .withIcon(WhispererForLaravelIcon.LARAVEL_ICON)
                    );
                }
            }
            return true;
        }, project);

        return variants.toArray();
    }
}

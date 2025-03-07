package at.alirezamoh.whisperer_for_laravel.eloquent;

import at.alirezamoh.whisperer_for_laravel.support.WhispererForLaravelIcon;
import at.alirezamoh.whisperer_for_laravel.support.utils.*;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.codeInsight.navigation.impl.PsiTargetPresentationRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class EloquentModelLineMarker extends RelatedItemLineMarkerProvider {
    /**
     * Collects navigation markers for a given PSI element if it represents an Eloquent model
     *
     * @param element the PSI element to inspect
     * @param result  the collection to add marker info to
     */
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        Project project = element.getProject();

        if (element instanceof PhpClass phpClass && EloquentUtils.isEloquentModel(phpClass, project)) {
            PsiElement classNameIdentifier = phpClass.getNameIdentifier();
            if (classNameIdentifier == null) {
                return;
            }

            createLineMarker(
                result,
                ModelRelatedFilesCollector.extractRelatedFiles(project, phpClass),
                classNameIdentifier
            );
        }
    }

    /**
     * Creates and adds a navigation line marker
     *
     * @param result              the collection to add the line marker info into
     * @param migrations          the list of migration (and factory) files to navigate to
     * @param classNameIdentifier the PSI element representing the model's name
     */
    private void createLineMarker(@NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result, List<PsiFile> migrations, PsiElement classNameIdentifier) {
        NavigationGutterIconBuilder<PsiElement> builder =
            NavigationGutterIconBuilder.create(WhispererForLaravelIcon.LARAVEL_ICON)
                .setTargets(migrations)
                .setPopupTitle("Related Files")
                .setTargetRenderer(this::getPsiTargetPresentationRenderer)
                .setTooltipText("Navigate to related files");

        result.add(builder.createLineMarkerInfo(classNameIdentifier));
    }

    /**
     * Returns a target presentation renderer that displays the name of a PSI file
     *
     * @return a renderer that extracts and displays the file name from a {@code PsiFile}
     */
    private @NotNull PsiTargetPresentationRenderer<PsiElement> getPsiTargetPresentationRenderer() {
        return new PsiTargetPresentationRenderer<>() {
            @Override
            public @Nls @NotNull String getElementText(@NotNull PsiElement element) {
                if (element instanceof PsiFile) {
                    return ((PsiFile) element).getName();
                }

                return super.getElementText(element);
            }

            @Override
            public @Nls @Nullable String getContainerText(@NotNull PsiElement element) {
                return null;
            }
        };
    }
}

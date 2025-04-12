package at.alirezamoh.whisperer_for_laravel.packages.livewire.component;

import at.alirezamoh.whisperer_for_laravel.support.WhispererForLaravelIcon;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.codeInsight.navigation.impl.PsiTargetPresentationRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class LivewireLineMarkerUtil {
    /**
     * Creates and adds a navigation line marker
     *
     * @param result              the collection to add the line marker info into
     * @param files               the list of related files
     * @param classNameIdentifier the PSI element representing the model's name
     */
    public static void createLineMarker(@NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result, List<PsiFile> files, PsiElement classNameIdentifier) {
        if (files.isEmpty()) {
            return;
        }

        NavigationGutterIconBuilder<PsiElement> builder =
            NavigationGutterIconBuilder.create(WhispererForLaravelIcon.LARAVEL_ICON)
                .setTargets(files)
                .setPopupTitle("Related Files")
                .setTargetRenderer(LivewireLineMarkerUtil::getPsiTargetPresentationRenderer)
                .setTooltipText("Navigate to related files");

        result.add(builder.createLineMarkerInfo(classNameIdentifier));
    }

    /**
     * Returns a target presentation renderer that displays the name of a PSI file
     *
     * @return a renderer that extracts and displays the file name from a {@code PsiFile}
     */
    private static @NotNull PsiTargetPresentationRenderer<PsiElement> getPsiTargetPresentationRenderer() {
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

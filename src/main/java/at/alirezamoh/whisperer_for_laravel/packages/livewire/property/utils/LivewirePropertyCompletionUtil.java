package at.alirezamoh.whisperer_for_laravel.packages.livewire.property.utils;

import at.alirezamoh.whisperer_for_laravel.packages.livewire.LivewireUtil;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class LivewirePropertyCompletionUtil {
    private LivewirePropertyCompletionUtil() {}

    /**
     * Attempts to add Livewire property completions if the host matches a given condition.
     *
     * @param parameters The completion parameters provided by the IDE.
     * @param resultSet  The result set where lookup elements should be added.
     * @param hostFilter A predicate that determines if the {@link PsiLanguageInjectionHost} is valid.
     * @param propertyCollector A processor that collects {@link LookupElementBuilder} items
     *                          (for example, calling {@code LivewirePropertyProvider.collectProperties}).
     */
    public static void addCompletionsIfHostMatches(
        @NotNull CompletionParameters parameters,
        @NotNull CompletionResultSet resultSet,
        @NotNull Predicate<PsiLanguageInjectionHost> hostFilter,
        @NotNull BiConsumer<PsiFile, CompletionResultSet> propertyCollector
    ) {
        PsiElement position = parameters.getPosition().getOriginalElement();
        Project project = position.getProject();

        // Abort if we should not complete or navigate
        if (LivewireUtil.doNotCompleteOrNavigate(project)) {
            return;
        }

        PsiElement originalPosition = parameters.getOriginalPosition();
        PsiFile originalFile = parameters.getOriginalFile();

        if (
            originalPosition != null
            && InjectedLanguageManager.getInstance(originalFile.getProject()).isInjectedFragment(originalFile)
        ) {

            PsiLanguageInjectionHost host = InjectedLanguageManager.getInstance(project).getInjectionHost(originalPosition);

            if (host != null && hostFilter.test(host)) {

                String prefix = resultSet.getPrefixMatcher().getPrefix();
                if (prefix.startsWith("$")) {
                    resultSet = resultSet.withPrefixMatcher(prefix.substring(1));
                }

                CompletionResultSet finalResult = resultSet;

                propertyCollector.accept(host.getContainingFile(), finalResult);
            }
        }
    }

    /**
     * Convenience method that calls {@link LivewirePropertyProvider#collectProperties} on the given file,
     * and adds them to the {@link CompletionResultSet}.
     *
     * @param project The current project.
     * @param file The file in which to collect properties.
     * @param resultSet The {@link CompletionResultSet} to add the collected items to.
     */
    public static void collectAndAddProperties(
        @NotNull Project project,
        @Nullable PsiFile file,
        @NotNull CompletionResultSet resultSet
    ) {
        if (file == null) {
            return;
        }
        List<LookupElementBuilder> variants =
            LivewirePropertyProvider.collectProperties(project, file, false);

        if (variants != null) {
            resultSet.addAllElements(variants);
        }
    }
}

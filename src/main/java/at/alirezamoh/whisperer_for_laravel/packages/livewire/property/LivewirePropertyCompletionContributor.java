package at.alirezamoh.whisperer_for_laravel.packages.livewire.property;

import at.alirezamoh.whisperer_for_laravel.packages.livewire.property.utils.LivewirePropertyCompletionUtil;
import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.blade.psi.BladePsiLanguageInjectionHost;
import com.jetbrains.php.lang.PhpLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * A CompletionContributor that provides property suggestions
 * for the Livewire PHP class associated with a Blade template file in the livewire blade file
 */
public class LivewirePropertyCompletionContributor extends CompletionContributor {
    public LivewirePropertyCompletionContributor() {
        //{{ $user }}
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(PhpLanguage.INSTANCE),
            new CompletionProvider<>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                    LivewirePropertyCompletionUtil.addCompletionsIfHostMatches(
                        parameters,
                        resultSet,
                        host -> host instanceof BladePsiLanguageInjectionHost,

                        (file, finalResult) -> {
                            Project project = file.getProject();
                            LivewirePropertyCompletionUtil.collectAndAddProperties(project, file, finalResult);
                        }
                    );
                }
            }
        );
    }
}


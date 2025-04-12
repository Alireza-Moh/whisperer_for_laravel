package at.alirezamoh.whisperer_for_laravel.packages.livewire.property;

import at.alirezamoh.whisperer_for_laravel.packages.livewire.property.utils.LivewirePropertyCompletionUtil;
import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A CompletionContributor that provides property suggestions
 * for the Livewire PHP class associated with a Blade template file in the livewire blade file
 */
public class LivewirePropertyInInlineCompletionContributor extends CompletionContributor {
    public LivewirePropertyInInlineCompletionContributor() {
        //{{ $user }}
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(XmlPatterns.xmlText())
                .andNot(PlatformPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue())),
            new CompletionProvider<>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                    LivewirePropertyCompletionUtil.addCompletionsIfHostMatches(
                        parameters,
                        resultSet,
                        host -> host instanceof StringLiteralExpression stringLiteral
                            && Objects.equals(stringLiteral.getName(), "HTML"),
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


package at.alirezamoh.whisperer_for_laravel.packages.livewire.htmlDirectives;
import at.alirezamoh.whisperer_for_laravel.packages.livewire.LivewireUtil;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.blade.BladeFileType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LivewireHtmlDirectiveCompletionContributor extends CompletionContributor {
    private final List<String> DIRECTIVES = List.of(
        "wire:click",
        "wire:submit",
        "wire:navigate",
        "wire:model",
        "wire:loading",
        "wire:current",
        "wire:dirty",
        "wire:confirm",
        "wire:transition",
        "wire:init",
        "wire:poll",
        "wire:offline",
        "wire:ignore",
        "wire:replace",
        "wire:stream"
    );

    public LivewireHtmlDirectiveCompletionContributor() {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(XmlPatterns.xmlAttribute())
                .andNot(PlatformPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()))
                .inVirtualFile(PlatformPatterns.virtualFile().ofType(BladeFileType.INSTANCE)),
            new CompletionProvider<>() {

                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {

                    Project project = completionParameters.getPosition().getProject();
                    if (LivewireUtil.doNotCompleteOrNavigate(project)) {
                        return;
                    }

                    for (String directive : DIRECTIVES) {
                        LookupElementBuilder lookupElementBuilder = PsiElementUtils.buildSimpleLookupElement(directive)
                            .withInsertHandler(new XmlAttributeInsertHandler());

                        completionResultSet.addElement(
                            PsiElementUtils.buildPrioritizedLookupElement(lookupElementBuilder, 100)
                        );
                    }
                }
            }
        );
    }
}

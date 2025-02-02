package at.alirezamoh.whisperer_for_laravel.packages.livewire.htmlDirectives;
import at.alirezamoh.whisperer_for_laravel.packages.livewire.LivewireUtil;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

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
        "wire:stream",
        "wire:keydown",
        "wire:keyup",
        "wire:mouseenter"
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

                    buildSuggestions(completionResultSet);
                }
            }
        );

        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(XmlPatterns.xmlAttribute())
                .andNot(PlatformPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue())),
            new CompletionProvider<>() {

                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                    PsiElement position = completionParameters.getPosition();
                    Project project = position.getProject();
                    if (LivewireUtil.doNotCompleteOrNavigate(project)) {
                        return;
                    }

                    PsiElement originalPosition = completionParameters.getOriginalPosition();
                    PsiFile originalFile = completionParameters.getOriginalFile();
                    if (originalPosition == null) {
                        return;
                    }

                    if (!InjectedLanguageManager.getInstance(originalFile.getProject()).isInjectedFragment(originalFile)) {
                        return;
                    }

                    PsiLanguageInjectionHost host = LivewireUtil.getFromPsiLanguageInjectionHost(project, originalPosition);
                    if (!(host instanceof StringLiteralExpression stringLiteralExpression)) {
                        return;
                    }

                    if (!Objects.equals(stringLiteralExpression.getName(), "HTML")) {
                        return;
                    }

                    Method renderMethod = PsiTreeUtil.getParentOfType(stringLiteralExpression, Method.class);
                    if (renderMethod != null && "render".equals(renderMethod.getName())) {
                        buildSuggestions(completionResultSet);
                    }
                }
            }
        );
    }

    private void buildSuggestions(@NotNull CompletionResultSet completionResultSet) {
        for (String directive : DIRECTIVES) {
            LookupElementBuilder lookupElementBuilder = PsiElementUtils.buildSimpleLookupElement(directive)
                .withInsertHandler(new XmlAttributeInsertHandler());

            completionResultSet.addElement(
                PsiElementUtils.buildPrioritizedLookupElement(lookupElementBuilder, 100)
            );
        }
    }
}

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
    
    private final List<String> KEY_DIRECTIVES = List.of(
        "wire:keydown",
        "wire:keyup"
    );

    private final List<String> KEYS = List.of(
        "shift",
        "enter",
        "space",
        "ctrl",
        "cmd",
        "meta",
        "alt",
        "up",
        "down",
        "left",
        "right",
        "escape",
        "tab",
        "caps-lock",
        "equal",
        "period",
        "slash",
        "prevent",
        "stop",
        "window",
        "outside",
        "document",
        "once",
        "debounce",
        "debounce.100ms",
        "throttle",
        "throttle.100ms",
        "self",
        "camel",
        "dot",
        "passive",
        "capture"
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

        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().inside(XmlPatterns.xmlAttribute())
                .andNot(PlatformPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()))
                .inVirtualFile(PlatformPatterns.virtualFile().ofType(BladeFileType.INSTANCE)),
            new CompletionProvider<>() {

                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {

                    PsiElement psiElement = completionParameters.getPosition();
                    Project project = psiElement.getProject();
                    if (LivewireUtil.doNotCompleteOrNavigate(project)) {
                        return;
                    }

                    buildSuggestionsForKeys(completionParameters, completionResultSet, psiElement);
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
                        buildSuggestionsForKeys(completionParameters, completionResultSet, position);
                    }
                }
            }
        );
    }

    private void buildSuggestionsForKeys(@NotNull CompletionParameters completionParameters, @NotNull CompletionResultSet completionResultSet, PsiElement psiElement) {
        CompletionResultSet resultOnPipe = getCompletionResultSetOnDot(psiElement, completionResultSet, completionParameters);
        if (resultOnPipe != null) {
            completionResultSet = resultOnPipe;
        }

        for (String key : KEYS) {
            LookupElementBuilder lookupElementBuilder = PsiElementUtils.buildSimpleLookupElement(key);

            completionResultSet.addElement(
                PsiElementUtils.buildPrioritizedLookupElement(lookupElementBuilder, 1000)
            );
        }
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

    /**
     * Returns a new CompletionResultSet with a prefix matcher based on the text after the last dot symbol
     * This method checks if the current PSI element's text contains a dot symbol (|).
     * If it does, it creates a new CompletionResultSet with a prefix matcher that matches
     * the text after the last dot symbol. This allows for accurate completion suggestions
     * when the user is typing validation rules separated by dots
     * @return The new CompletionResultSet with a prefix matcher, or null if no dot symbol is found
     */
    private CompletionResultSet getCompletionResultSetOnDot(PsiElement psiElement, CompletionResultSet currentCompletionResult, CompletionParameters completionParameters) {
        String text = psiElement.getText();
        CompletionResultSet newCompletionResult = null;

        if (text.contains(".")) {
            int dotIndex = text.lastIndexOf('.', completionParameters.getOffset() - psiElement.getTextRange().getStartOffset() - 1);
            String newText = text.substring(dotIndex + 1, completionParameters.getOffset() - psiElement.getTextRange().getStartOffset());

            newCompletionResult = currentCompletionResult.withPrefixMatcher(newText);
        }

        return newCompletionResult;
    }
}

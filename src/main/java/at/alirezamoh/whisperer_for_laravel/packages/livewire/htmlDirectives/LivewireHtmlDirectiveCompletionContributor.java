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

    private final List<String> ACTION_DIRECTIVES = List.of(
        "wire:keydown",
        "wire:submit",
        "wire:click",
        "wire:keydown",
        "wire:keyup",
        "wire:mouseenter",
        "wire:load",
        "wire:unload",
        "wire:beforeunload",
        "wire:resize",
        "wire:scroll",
        "wire:error",
        "wire:abort",
        "wire:copy",
        "wire:cut",
        "wire:paste",
        "wire:play",
        "wire:playing",
        "wire:pause",
        "wire:ended",
        "wire:volumechange",
        "wire:loadstart",
        "wire:loadeddata",
        "wire:loadedmetadata",
        "wire:timeupdate",
        "wire:durationchange",
        "wire:progress",
        "wire:suspend",
        "wire:waiting",
        "wire:reset",
        "wire:change",
        "wire:input",
        "wire:invalid",
        "wire:formdata",
        "wire:focus",
        "wire:blur",
        "wire:focusin",
        "wire:focusout",
        "wire:dblclick",
        "wire:mousedown",
        "wire:mouseup",
        "wire:mousemove",
        "wire:mouseover",
        "wire:mouseout",
        "wire:mouseleave",
        "wire:contextmenu",
        "wire:auxclick",
        "wire:wheel",
        "wire:keypress",
        "wire:touchstart",
        "wire:touchend",
        "wire:touchmove",
        "wire:touchcancel",
        "wire:pointerover",
        "wire:pointerenter",
        "wire:pointerdown",
        "wire:pointermove",
        "wire:pointerup",
        "wire:pointercancel",
        "wire:pointerout",
        "wire:pointerleave",
        "wire:drag",
        "wire:dragstart",
        "wire:dragend",
        "wire:dragenter",
        "wire:dragover",
        "wire:dragleave",
        "wire:drop",
        "wire:animationstart",
        "wire:animationend",
        "wire:animationiteration",
        "wire:transitionend"
    );

    private final List<String> THIRD_PARTY_TRIX_TEXT_EDITOR_ACTION_DIRECTIVES = List.of(
        "wire:trix-before-initialize",
        "wire:trix-initialize",
        "wire:trix-change",
        "wire:trix-selection-change",
        "wire:trix-focus",
        "wire:trix-blur",
        "wire:trix-paste",
        "wire:trix-file-accept",
        "wire:trix-attachment-add",
        "wire:trix-attachment-remove"
    );

    private final List<String> MODIFIERS = List.of(
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

    private final List<String> WIRE_MODEL_MODIFIERS = List.of(
        "live",
        "blur",
        "change",
        "lazy",
        "debounce.[?]ms",
        "throttle.[?]ms",
        "number",
        "boolean",
        "fill"
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
                    if (LivewireUtil.shouldNotCompleteOrNavigate(project)) {
                        return;
                    }

                    buildSuggestions(completionResultSet, project);
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
                    if (LivewireUtil.shouldNotCompleteOrNavigate(project)) {
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
                        buildSuggestions(completionResultSet, project);
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
                    if (LivewireUtil.shouldNotCompleteOrNavigate(project)) {
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
                    if (LivewireUtil.shouldNotCompleteOrNavigate(project)) {
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

        for (String key : MODIFIERS) {
            LookupElementBuilder lookupElementBuilder = PsiElementUtils.buildSimpleLookupElement(key);

            completionResultSet.addElement(
                PsiElementUtils.buildPrioritizedLookupElement(lookupElementBuilder, 1000)
            );
        }

        if (psiElement.getText().startsWith("wire:model")) {
            for (String key : WIRE_MODEL_MODIFIERS) {
                LookupElementBuilder lookupElementBuilder = PsiElementUtils.buildSimpleLookupElement(key);

                completionResultSet.addElement(
                    PsiElementUtils.buildPrioritizedLookupElement(lookupElementBuilder, 1000)
                );
            }
        }
    }

    private void buildSuggestions(@NotNull CompletionResultSet completionResultSet, Project project) {
        for (String directive : DIRECTIVES) {
            LookupElementBuilder lookupElementBuilder = PsiElementUtils.buildSimpleLookupElement(directive)
                .withInsertHandler(new XmlAttributeInsertHandler());

            completionResultSet.addElement(
                PsiElementUtils.buildPrioritizedLookupElement(lookupElementBuilder, 100)
            );
        }

        for (String directive : ACTION_DIRECTIVES) {
            LookupElementBuilder lookupElementBuilder = PsiElementUtils.buildSimpleLookupElement(directive)
                .withInsertHandler(new XmlAttributeInsertHandler());

            completionResultSet.addElement(
                PsiElementUtils.buildPrioritizedLookupElement(lookupElementBuilder, 100)
            );
        }

        if (LivewireUtil.doesProjectUseTrixPackage(project)) {
            for (String directive : THIRD_PARTY_TRIX_TEXT_EDITOR_ACTION_DIRECTIVES) {
                LookupElementBuilder lookupElementBuilder = PsiElementUtils.buildSimpleLookupElement(directive)
                    .withInsertHandler(new XmlAttributeInsertHandler());

                completionResultSet.addElement(
                    PsiElementUtils.buildPrioritizedLookupElement(lookupElementBuilder, 100)
                );
            }
        }
    }

    /**
     * Returns a new CompletionResultSet with a prefix matcher based on the text after the last dot symbol
     * This method checks if the current PSI element's text contains a dot symbol (.).
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

package at.alirezamoh.whisperer_for_laravel.packages.livewire.actions;

import at.alirezamoh.whisperer_for_laravel.packages.livewire.LivewireUtil;
import at.alirezamoh.whisperer_for_laravel.packages.livewire.property.utils.LivewirePropertyProvider;
import at.alirezamoh.whisperer_for_laravel.support.MethodLookupElement;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.blade.html.BladeHtmlFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class LivewireActionDataBindingCompletionContributor extends CompletionContributor {
    private static final String[] PREDEFINED_ACTIONS = {
        "$refresh",
        "$commit",
        "$toggle",
        "$set",
        "$parent",
        "$dispatch"
    };

    public LivewireActionDataBindingCompletionContributor() {
        ElementPattern<? extends XmlAttribute> wireActionAttributePattern =
            PlatformPatterns.psiElement(XmlAttribute.class)
                .withName(
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

        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(
                PlatformPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()).withParent(
                    PlatformPatterns.psiElement(XmlAttributeValue.class)
                        .withParent(wireActionAttributePattern)
                ).inVirtualFile(PlatformPatterns.virtualFile().ofType(BladeFileType.INSTANCE)),
                PlatformPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()).withParent(
                    PlatformPatterns.psiElement(XmlAttributeValue.class)
                        .withParent(wireActionAttributePattern)
                ).inVirtualFile(PlatformPatterns.virtualFile().ofType(HtmlFileType.INSTANCE))
            ),
            new CompletionProvider<>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                    collectActions(
                        completionParameters.getPosition(),
                        completionResultSet
                    );
                }
            }
        );
    }

    /**
     * Gathers all available methods (as actions)
     * from Livewire component classes linked to the given Blade file
     *
     * @param element Current element
     * @param completionResultSet Completion result set to which we add the suggestions
     */
    public static void collectActions(PsiElement element, CompletionResultSet completionResultSet) {
        Project project = element.getProject();
        PsiFile originalFile = LivewireUtil.getFileFromPsiLanguageInjectionHost(project, element);
        if (originalFile == null) {
            return;
        }

        if (originalFile.getFileType() instanceof BladeHtmlFileType)  {
            Collection<PhpClass> phpClasses = LivewirePropertyProvider.findLivewireClasses(project, originalFile);
            if (phpClasses != null) {
                addMethodsFromPhpClasses(phpClasses, completionResultSet);
            }
            addPredefinedActions(completionResultSet);
        }

        if (originalFile instanceof PhpFile phpFile) {
            Collection<PhpClass> phpClasses = PhpClassUtils.getPhpClassesFromFile(phpFile);

            phpClasses.stream().findFirst().ifPresent(phpClass ->
                addMethodsFromPhpClasses(Collections.singleton(phpClass), completionResultSet)
            );
            addPredefinedActions(completionResultSet);
        }
    }

    /**
     * Adds lookup elements for all public methods of the given PHP classes
     *
     * @param phpClasses A collection of PHP classes to process
     * @param completionResultSet The completion result set where lookup elements are added
     */
    private static void addMethodsFromPhpClasses(Collection<PhpClass> phpClasses, CompletionResultSet completionResultSet) {
        for (PhpClass phpClass : phpClasses) {
            PhpClassUtils.getClassPublicMethods(phpClass, true).forEach(method -> {
                LookupElement lookupElement = PrioritizedLookupElement.withPriority(
                    new MethodLookupElement(method),
                    1000.0
                );
                completionResultSet.addElement(lookupElement);
            });
        }
    }

    /**
     * Adds predefined actions as lookup elements to the completion result set
     *
     * @param completionResultSet The completion result set where lookup elements are added
     */
    private static void addPredefinedActions(CompletionResultSet completionResultSet) {
        for (String predefinedAction : PREDEFINED_ACTIONS) {
            LookupElement lookupElement = PsiElementUtils.buildPrioritizedLookupElement(
                PsiElementUtils.buildSimpleLookupElement(predefinedAction),
                1000.0
            );
            completionResultSet.addElement(lookupElement);
        }
    }
}

package at.alirezamoh.whisperer_for_laravel.packages.livewire.actions;

import at.alirezamoh.whisperer_for_laravel.packages.livewire.LivewireUtil;
import at.alirezamoh.whisperer_for_laravel.packages.livewire.property.utils.LivewirePropertyProvider;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.project.Project;
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
import java.util.Objects;

public class LivewireActionDataBindingCompletionContributor extends CompletionContributor {
    private static final String[] PREDEFINED_ACTIONS = {
        "$refresh",
        "$commit"
    };
    public LivewireActionDataBindingCompletionContributor() {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(
                PlatformPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()).withParent(
                    PlatformPatterns.psiElement(XmlAttributeValue.class)
                        .withParent(
                            PlatformPatterns.or(
                                PlatformPatterns.psiElement(XmlAttribute.class)
                                    .withName("wire:submit"),
                                PlatformPatterns.psiElement(XmlAttribute.class)
                                    .withName("wire:click")
                            )
                        )
                ).inVirtualFile(PlatformPatterns.virtualFile().ofType(BladeFileType.INSTANCE)),
                PlatformPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()).withParent(
                    PlatformPatterns.psiElement(XmlAttributeValue.class)
                        .withParent(
                            PlatformPatterns.or(
                                PlatformPatterns.psiElement(XmlAttribute.class)
                                    .withName("wire:submit"),
                                PlatformPatterns.psiElement(XmlAttribute.class)
                                    .withName("wire:click")
                            )
                        )
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
            PhpClassUtils.getClassPublicMethods(phpClass, true).forEach(method ->
                addLookupElement(method.getName(), completionResultSet)
            );
        }
    }

    /**
     * Creates a lookup element for the given name and adds it to the completion result set
     *
     * @param name The name to be used in the lookup element
     * @param completionResultSet The completion result set where the lookup element is added
     */
    private static void addLookupElement(String name, CompletionResultSet completionResultSet) {
        LookupElement lookupElement = PsiElementUtils.buildPrioritizedLookupElement(
            PsiElementUtils.buildSimpleLookupElement(name),
            1000
        );
        completionResultSet.addElement(lookupElement);
    }

    /**
     * Adds predefined actions as lookup elements to the completion result set
     *
     * @param completionResultSet The completion result set where lookup elements are added
     */
    private static void addPredefinedActions(CompletionResultSet completionResultSet) {
        for (String predefinedAction : PREDEFINED_ACTIONS) {
            addLookupElement(predefinedAction, completionResultSet);
        }
    }
}

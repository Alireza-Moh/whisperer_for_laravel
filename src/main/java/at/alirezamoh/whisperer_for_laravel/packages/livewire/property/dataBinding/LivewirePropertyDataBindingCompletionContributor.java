package at.alirezamoh.whisperer_for_laravel.packages.livewire.property.dataBinding;

import at.alirezamoh.whisperer_for_laravel.packages.livewire.LivewireUtil;
import at.alirezamoh.whisperer_for_laravel.packages.livewire.property.utils.LivewirePropertyProvider;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.blade.BladeFileType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LivewirePropertyDataBindingCompletionContributor extends CompletionContributor {
    public LivewirePropertyDataBindingCompletionContributor() {
        ElementPattern<? extends XmlAttribute> wireModelAttributePattern =
            PlatformPatterns.psiElement(XmlAttribute.class)
                .withName(
                    "wire:model",
                    "wire:model.live",
                    "wire:model.blur",
                    "wire:model.change",
                    "wire:model.lazy",
                    "wire:model.debounce.[?]ms",
                    "wire:model.throttle.[?]ms",
                    "wire:model.number",
                    "wire:model.boolean",
                    "wire:model.fill"
                );
        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(
                PlatformPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()).withParent(
                    PlatformPatterns.psiElement(XmlAttributeValue.class)
                        .withParent(wireModelAttributePattern)
                ).inVirtualFile(PlatformPatterns.virtualFile().ofType(BladeFileType.INSTANCE)),
                PlatformPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()).withParent(
                    PlatformPatterns.psiElement(XmlAttributeValue.class)
                        .withParent(wireModelAttributePattern)
                ).inVirtualFile(PlatformPatterns.virtualFile().ofType(HtmlFileType.INSTANCE))
            ),
            new CompletionProvider<>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                    PsiElement position = parameters.getPosition().getOriginalElement();

                    Project project = position.getProject();
                    if (LivewireUtil.shouldNotCompleteOrNavigate(project)) {
                        return;
                    }

                    List<LookupElementBuilder> variants = LivewirePropertyProvider.collectProperties(
                        project,
                        position.getContainingFile(),
                        true
                    );

                    if (variants != null) {
                        resultSet.addAllElements(variants);
                    }
                }
            }
        );
    }
}

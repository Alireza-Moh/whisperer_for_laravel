package at.alirezamoh.whisperer_for_laravel.packages.livewire.property.dataBinding;

import at.alirezamoh.whisperer_for_laravel.packages.livewire.LivewireUtil;
import at.alirezamoh.whisperer_for_laravel.packages.livewire.property.utils.LivewirePropertyProvider;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LivewirePropertyDataBindingGoToDeclarationHandler implements GotoDeclarationHandler {
    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement psiElement, int i, Editor editor) {
        if (psiElement == null) {
            return null;
        }

        Project project = psiElement.getProject();

        if (LivewireUtil.doNotCompleteOrNavigate(project)) {
            return null;
        }

        XmlAttributeValue attributeValue = PsiTreeUtil.getParentOfType(psiElement, XmlAttributeValue.class, false);
        if (attributeValue != null && attributeValue.getParent() instanceof XmlAttribute xmlAttribute && xmlAttribute.getName().equals("wire:model")) {
            PsiFile originalFile = psiElement.getContainingFile();

            List<PsiElement> resolvedProperties = LivewirePropertyProvider.resolveProperty(
                project,
                originalFile,
                StrUtils.removeQuotes(attributeValue.getValue()),
                true
            );

            if (resolvedProperties == null) {
                return PsiElement.EMPTY_ARRAY;
            }

            return resolvedProperties.toArray(new PsiElement[0]);
        }

        return null;
    }
}

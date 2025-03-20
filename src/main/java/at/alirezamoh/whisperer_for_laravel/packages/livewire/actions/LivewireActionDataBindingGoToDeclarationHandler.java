package at.alirezamoh.whisperer_for_laravel.packages.livewire.actions;

import at.alirezamoh.whisperer_for_laravel.packages.livewire.LivewireUtil;
import at.alirezamoh.whisperer_for_laravel.packages.livewire.property.utils.LivewirePropertyProvider;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.jetbrains.php.blade.html.BladeHtmlFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LivewireActionDataBindingGoToDeclarationHandler implements GotoDeclarationHandler {
    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement psiElement, int i, Editor editor) {
        if (psiElement == null) {
            return null;
        }

        Project project = psiElement.getProject();

        if (LivewireUtil.shouldNotCompleteOrNavigate(project)) {
            return null;
        }

        XmlAttributeValue attributeValue = PsiTreeUtil.getParentOfType(psiElement, XmlAttributeValue.class, false);
        if (
            attributeValue != null
                && attributeValue.getParent() instanceof XmlAttribute xmlAttribute
                && (xmlAttribute.getName().equals("wire:click") || xmlAttribute.getName().equals("wire:submit"))
        ) {
            PsiFile originalFile = psiElement.getContainingFile();

            return Objects.requireNonNull(
                resolveActions(project, originalFile, StrUtils.removeQuotes(attributeValue.getValue()))
            ).toArray(new PsiElement[0]);
        }

        return null;
    }

    /**
     * Resolve an action
     *
     * @param project      Current project
     * @param originalFile Blade file from which we infer the Livewire component name
     */
    public static @Nullable List<Method> resolveActions(Project project, PsiFile originalFile, String actionName) {
        List<Method> methods = new ArrayList<>();
        actionName = StrUtils.removeQuotes(actionName);

        Collection<PhpClass> phpClasses = resolvePhpClasses(project, originalFile);
        if (phpClasses == null) {
            return null;
        }

        String finalActionName = actionName;
        phpClasses.forEach(phpClass ->
            PhpClassUtils.getClassPublicMethods(phpClass, true).stream()
                .filter(method -> finalActionName.equals(method.getName()))
                .forEach(methods::add)
        );

        return methods;
    }

    /**
     * Determines the relevant PHP classes from the given file
     *
     * <p>If the file type is a Blade file, the Livewire classes are looked up. Otherwise,
     * if the file contains an injection host that is a string literal, then the containing PHP file
     * is used and only the first PHP class (if available) is considered.</p>
     *
     * @param project      Current project
     * @param originalFile File to resolve classes from
     * @return A collection of PHP classes, or null if no appropriate classes could be determined
     */
    private static @Nullable Collection<PhpClass> resolvePhpClasses(Project project, PsiFile originalFile) {
        FileType fileType = originalFile.getFileType();

        if (fileType instanceof BladeHtmlFileType) {
            return LivewirePropertyProvider.findLivewireClasses(project, originalFile);
        } else {
            PsiLanguageInjectionHost injectionHost = LivewireUtil.getFromPsiLanguageInjectionHost(project, originalFile);
            if (injectionHost instanceof StringLiteralExpression) {
                PsiFile phpFile = injectionHost.getContainingFile();
                if (phpFile instanceof PhpFile) {
                    Collection<PhpClass> phpClasses = PhpClassUtils.getPhpClassesFromFile((PhpFile) phpFile);
                    return phpClasses.stream().findFirst().map(Collections::singletonList).orElse(null);
                }
            }
        }

        return null;
    }
}

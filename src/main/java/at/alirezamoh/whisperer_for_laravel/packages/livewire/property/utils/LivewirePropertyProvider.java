package at.alirezamoh.whisperer_for_laravel.packages.livewire.property.utils;

import at.alirezamoh.whisperer_for_laravel.support.utils.PhpIndexUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.blade.BladeFileType;
import com.jetbrains.php.blade.html.BladeHtmlFileType;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class LivewirePropertyProvider {
    /**
     * Gathers all available properties (fields and string parameters to <code>with()</code>)
     * from Livewire component classes linked to the given Blade file
     *
     * @param project      Current project
     * @param originalFile Blade file from which we infer the Livewire component name.
     * @return A list of lookup suggestions for the identified properties, or <code>null</code> if no Livewire class is found
     */
    public static @Nullable List<LookupElementBuilder> collectProperties(Project project, PsiFile originalFile, boolean ignoreWithMethod) {
        LivewirePhpComponentVisitor visitor = new LivewirePhpComponentVisitor(ignoreWithMethod);

        //completion for inline html
        if (originalFile instanceof PhpFile) {
            originalFile.acceptChildren(visitor);

            return buildLookupElementsFromProperties(visitor.properties);
        }

        //completion for blade files
        FileType fileType = originalFile.getFileType();
        if (fileType instanceof BladeHtmlFileType || fileType instanceof BladeFileType) {
            Collection<PhpClass> phpClasses = findLivewireClasses(project, originalFile);
            if (phpClasses == null) {
                return null;
            }

            for (PhpClass phpClass : phpClasses) {
                phpClass.acceptChildren(visitor);
            }

            return buildLookupElementsFromProperties(visitor.properties);
        }

        return null;
    }

    /**
     * Builds corresponding
     * {@link LookupElementBuilder} items that represent Livewire properties from a list of fields
     *
     * @param properties The class fields {@link Field} or {@link StringLiteralExpression}
     * @return A list of suggestions or an empty list if no properties found
     */
    private static @NotNull List<LookupElementBuilder> buildLookupElementsFromProperties(@NotNull Collection<PsiElement> properties) {
        List<LookupElementBuilder> variants = new ArrayList<>();
        for (PsiElement property : properties) {
            if (property instanceof Field field) {
                variants.add(PsiElementUtils.buildSimpleLookupElement(field.getName()));
            } else if (property instanceof StringLiteralExpression stringLiteral) {
                variants.add(
                    PsiElementUtils.buildSimpleLookupElement(
                        StrUtils.removeQuotes(stringLiteral.getText())
                    )
                );
            }
        }
        return variants;
    }

    /**
     * Finds the PSI elements that match a given property name in all related Livewire classes
     *
     * @param project      Current project
     * @param originalFile Blade file from which we infer the Livewire component name
     * @param propertyName Property name to look up
     * @return A list of matching PSI elements, or <code>null</code> if no Livewire class is found
     */
    public static List<PsiElement> resolveProperty(Project project, PsiFile originalFile, String propertyName, boolean ignoreWithMethod) {
        LivewirePhpComponentVisitor visitor = new LivewirePhpComponentVisitor(ignoreWithMethod);
        propertyName = StrUtils.removeQuotes(propertyName);

        if (originalFile.getName().endsWith(".blade.php")) {
            Collection<PhpClass> phpClasses = findLivewireClasses(project, originalFile);
            if (phpClasses == null) {
                return null;
            }

            for (PhpClass phpClass : phpClasses) {
                phpClass.accept(visitor);
            }

            return collectMatchingProperties(visitor.properties, propertyName);
        }

        if (originalFile.getFileType() == PhpFileType.INSTANCE) {
            originalFile.acceptChildren(visitor);

            return collectMatchingProperties(visitor.properties, propertyName);
        }

        return null;
    }

    /**
     * Iterates over the collected PSI elements and returns those that match the specified property name
     *
     * @param properties   The collection of PSI elements gathered by {@link LivewirePhpComponentVisitor}
     * @param propertyName The name of the property to match
     * @return A list of matching PSI elements (fields or string literals that match the name)
     */
    private static List<PsiElement> collectMatchingProperties(Collection<PsiElement> properties, String propertyName) {
        List<PsiElement> matches = new ArrayList<>();
        for (PsiElement property : properties) {
            if (property instanceof Field field && field.getName().equals(propertyName)) {
                matches.add(field);
            } else if (property instanceof StringLiteralExpression stringLiteral) {
                String value = StrUtils.removeQuotes(stringLiteral.getText());
                if (value.equals(propertyName)) {
                    matches.add(stringLiteral);
                }
            }
        }
        return matches;
    }

    /**
     * Finds the PhpClasses corresponding Livewire class
     * @param project The current project
     * @param originalFile the blade file user is in
     * @return List of all founded livewire phpClasses
     */
    public static @Nullable Collection<PhpClass> findLivewireClasses(Project project, PsiFile originalFile) {
        String fileName = originalFile.getName();
        if (!fileName.endsWith(".blade.php")) {
            return null;
        }

        String fileNameInCamelCase = StrUtils.camel(
            fileName.replace(".blade.php", ""),
            '-'
        );

        String className = StrUtils.ucFirst(fileNameInCamelCase);
        Collection<PhpClass> phpClasses = new ArrayList<>();

        for(PhpClass clazz : PhpIndexUtils.getPhpClassesByName(className, project)) {
            if (Objects.equals(clazz.getSuperFQN(), "\\Livewire\\Component")) {
                phpClasses.add(clazz);
            }
        }

        return phpClasses;
    }
}

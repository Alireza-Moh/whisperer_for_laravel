package at.alirezamoh.whisperer_for_laravel.eloquent.factroy;

import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.whisperer_for_laravel.support.utils.*;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class EloquentFieldInFactoryCompletionContributor extends CompletionContributor {
    public static final String BASE_FACTORY_CLASS_NAMESPACE = "\\Illuminate\\Database\\Eloquent\\Factories\\Factory";

    public static final String FACTORY_INIT_METHOD = "definition";

    EloquentFieldInFactoryCompletionContributor() {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL),
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE)
            ),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                    PsiElement psiElement = completionParameters.getPosition().getOriginalElement().getParent();
                    Project project = psiElement.getProject();


                    if (!PluginUtils.isLaravelProject(project) && PluginUtils.isLaravelFrameworkNotInstalled(project)) {
                        return;
                    }

                    MethodImpl methodCall = PsiTreeUtil.getParentOfType(psiElement, MethodImpl.class);
                    if (methodCall == null) {
                        return;
                    }

                    PhpClass factoryClass = methodCall.getContainingClass();

                    if (isInsideCorrectMethodMethod(methodCall, factoryClass, psiElement, project)) {
                        List<Field> fields = EloquentModelFieldExtractorInFactory.extract(factoryClass, project);

                        if (fields == null) {
                            return;
                        }

                        for (Field field : fields) {
                            LookupElementBuilder lookupElementBuilder = PsiElementUtils.buildSimpleLookupElement(field.getName());

                            completionResultSet.addElement(
                                PsiElementUtils.buildPrioritizedLookupElement(lookupElementBuilder, 1000)
                            );
                        }
                    }
                }
            }
        );
    }

    /**
     * Checks if the PSI element is inside a MethodImpl with the name 'definition'
     */
    private boolean isInsideCorrectMethodMethod(@NotNull MethodImpl methodCall, PhpClass factoryClass, PsiElement psiElement, Project project) {
        return PhpClassUtils.isChildOfBaseClass(factoryClass, project, BASE_FACTORY_CLASS_NAMESPACE)
            && methodCall.getName().equals(FACTORY_INIT_METHOD)
            && PsiElementUtils.isInsideArrayKey(psiElement);
    }
}
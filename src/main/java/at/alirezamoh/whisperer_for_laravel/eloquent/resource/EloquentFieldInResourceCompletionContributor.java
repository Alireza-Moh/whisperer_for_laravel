package at.alirezamoh.whisperer_for_laravel.eloquent.resource;

import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
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


public class EloquentFieldInResourceCompletionContributor extends CompletionContributor {
    public static final String BASE_RESOURCE_CLASS_NAMESPACE = "\\Illuminate\\Http\\Resources\\Json\\JsonResource";

    public static final String BASE_RESOURCE_COLLECTION_CLASS_NAMESPACE = "\\Illuminate\\Http\\Resources\\Json\\ResourceCollection";

    public static final String FACTORY_INIT_METHOD = "toArray";

    EloquentFieldInResourceCompletionContributor() {
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

                    PhpClass resourceClass = methodCall.getContainingClass();

                    if (isInsideCorrectMethodMethod(methodCall, resourceClass, psiElement, project)) {
                        List<Field> fields = EloquentModelFieldExtractorInResource.extract(resourceClass, project);

                        if (fields == null) {
                            return;
                        }

                        for (Field field : fields) {
                            LookupElementBuilder lookupElementBuilder = PsiElementUtils.buildSimpleLookupElement(field.getName());

                            completionResultSet.addElement(
                                PsiElementUtils.buildPrioritizedLookupElement(lookupElementBuilder, 1000.0)
                            );
                        }
                    }
                }
            }
        );
    }

    /**
     * Checks if the PSI element is inside a MethodImpl with the name 'toArray'
     */
    private boolean isInsideCorrectMethodMethod(@NotNull MethodImpl methodCall, PhpClass factoryClass, PsiElement psiElement, Project project) {
        return PhpClassUtils.isChildOfBaseClass(factoryClass, project, BASE_RESOURCE_CLASS_NAMESPACE, BASE_RESOURCE_COLLECTION_CLASS_NAMESPACE)
            && methodCall.getName().equals(FACTORY_INIT_METHOD)
            && PsiElementUtils.isInsideArrayKey(psiElement);
    }
}
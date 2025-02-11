package at.alirezamoh.whisperer_for_laravel.eloquent.table;

import at.alirezamoh.whisperer_for_laravel.support.utils.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class TableReferenceContributor extends PsiReferenceContributor {
    /**
     * A list of fully qualified classes
     */
    private final static String[] QUERY_BUILDERS = {
        "\\Illuminate\\Support\\Facades\\DB",
        "\\Illuminate\\Database\\Query\\Builder",
        "\\Illuminate\\Database\\Eloquent\\Builder"
    };

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression.class).withParent(ParameterList.class),
            new PsiReferenceProvider() {

                @Override
                public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
                    Project project = psiElement.getProject();

                    if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    if (!(psiElement instanceof StringLiteralExpression stringLiteralExpression)) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    if (isInsideCorrectMethod(psiElement)) {
                        return new PsiReference[]{
                            new TableReference(
                                stringLiteralExpression,
                                new TextRange(
                                    PsiElementUtils.getStartOffset(stringLiteralExpression),
                                    PsiElementUtils.getEndOffset(stringLiteralExpression)
                                )
                            )
                        };
                    }

                    return new PsiReference[0];
                }
            }
        );
    }

    /**
     * Checks if the given PSI element is inside a recognized query builder method
     *
     * @param psiElement The PSI element representing a string literal
     * @return true or false
     */
    private boolean isInsideCorrectMethod(PsiElement psiElement) {
        MethodReference methodReference = MethodUtils.resolveMethodReference(psiElement, 10);

        return methodReference != null && PhpClassUtils.isCorrectRelatedClass(methodReference, psiElement.getProject(), QUERY_BUILDERS)
            && isTableMethod(methodReference)
            && isTableParam(methodReference, psiElement);
    }

    /**
     * Checks if the method name is one of the known table-related methods, e.g. "table", "from"
     *
     * @param methodReference The method reference to check
     * @return true or false
     */
    private boolean isTableMethod(MethodReference methodReference) {
        String methodName = methodReference.getName();

        if (methodName == null) {
            return false;
        }

        return LaravelPaths.DB_TABLE_METHODS.containsKey(methodName);
    }

    /**
     * Checks whether the position of the string argument matches the parameter index
     * required for table references in the recognized query builder methods
     *
     * @param method   The method reference
     * @param position The string literal PSI element
     * @return true or false
     */
    private boolean isTableParam(MethodReference method, PsiElement position) {
        int paramIndex = MethodUtils.findParamIndex(position, false);
        String methodName = method.getName();

        if (methodName == null) {
            return false;
        }

        Integer paramPosition = LaravelPaths.DB_TABLE_METHODS.get(methodName);
        return paramPosition == paramIndex;
    }
}

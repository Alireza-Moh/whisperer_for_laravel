package at.alirezamoh.whisperer_for_laravel.facade;

import at.alirezamoh.whisperer_for_laravel.facade.util.RealTimeFacadeUtil;
import at.alirezamoh.whisperer_for_laravel.support.WhispererForLaravelIcon;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides code completion for fields defined in FormRequest rules
 */
public class RealTimeFacadeCompletionContributor extends CompletionContributor {
    public RealTimeFacadeCompletionContributor() {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(
                PlatformPatterns.psiElement().withElementType(PhpTokenTypes.IDENTIFIER)
            ),
            new CompletionProvider<>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                    PsiElement position = parameters.getPosition().getOriginalElement();

                    Project project = position.getProject();
                    if (PluginUtils.shouldNotCompleteOrNavigate(project)) {
                        return;
                    }

                    String facadeFqn = getFacadeFQNFromStaticCall(position);
                    if ( facadeFqn != null && facadeFqn.startsWith("\\Facades")) {
                        getMethodsForCompletion(resultSet, project, facadeFqn);
                    }
                }
            }
        );
    }

    /**
     * Retrieves facade methods and adds them as completion lookup elements.
     *
     * @param resultSet  the completion result set to populate
     * @param project    the current project
     * @param facadeFqn  the fully qualified name of the facade
     */
    private void getMethodsForCompletion(@NotNull CompletionResultSet resultSet, Project project, String facadeFqn) {
        List<Method> facadeMethods = getFacadeMethods(project, facadeFqn);
        if (facadeMethods == null) {
            return;
        }

        for (Method method : facadeMethods) {
            String parameterList = Arrays.stream(method.getParameters())
                .map(Parameter::getText)
                .collect(Collectors.joining(", "));

            resultSet.addElement(
                createLookupElement(method, parameterList)
            );
        }
    }

    /**
     * Creates a lookup element for a method with its parameters and icon
     *
     * @param method         the method for which to create the lookup element
     * @param parameterList  a comma-separated list of the method's parameters
     * @return a configured LookupElementBuilder
     */
    private @NotNull LookupElementBuilder createLookupElement(Method method, String parameterList) {
        return LookupElementBuilder
            .create(method.getName())
            .withLookupString(method.getName())
            .withPresentableText(method.getName())
            .withTailText("(" + parameterList + ")", true)
            .bold()
            .withIcon(WhispererForLaravelIcon.LARAVEL_ICON);
    }

    /**
     * Traverses the PSI tree backward from the given position to get the class reference FQN
     *
     * @param position the current PSI element
     * @return the fully qualified name of the class, or null if not found
     */
    private @Nullable String getFacadeFQNFromStaticCall(PsiElement position) {
        PsiElement currentElement = position;
        ClassReference classReference = null;
        while (currentElement != null) {
            if (currentElement instanceof ClassReference) {
                classReference = (ClassReference) currentElement;
                break;
            }

            currentElement = currentElement.getPrevSibling();
        }

        if (classReference == null) {
            return null;
        }

        return classReference.getFQN();
    }

    /**
     * Retrieves the public methods of the facade class.
     *
     * @param project   the current project
     * @param facadeFqn the fully qualified name of the facade
     * @return a list of public methods of the facade, or null if the facade is not found
     */
    public @Nullable List<Method> getFacadeMethods(Project project, String facadeFqn) {
        PhpClass facade = RealTimeFacadeUtil.getFacadeClass(project, facadeFqn);

        if (facade == null) {
            return null;
        }

        return PhpClassUtils.getClassPublicMethods(facade, true);
    }
}


package at.alirezamoh.whisperer_for_laravel.routing.namespace;

import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PsiElementUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RouteNamespaceCompletionContributor extends CompletionContributor {
    /**
     * The namespaces of the `Route` facade and class
     */
    private final List<String> ROUTE_NAMESPACES = new ArrayList<>() {{
        add("\\Illuminate\\Routing\\Route");
        add("\\Illuminate\\Support\\Facades\\Route");
        add("\\Route");
        add("\\Illuminate\\Routing\\RouteRegistrar");
    }};

    /**
     * The names of the methods for autocompletion
     */
    public static Map<String, Integer> NAMESPACE_METHODS = new HashMap<>() {{
        put("namespace", 0);
    }};


    public RouteNamespaceCompletionContributor() {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL),
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE)
            ),
            new CompletionProvider<>() {

                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                    PsiElement element = completionParameters.getPosition().getOriginalElement();

                    MethodReference method = MethodUtils.resolveMethodReference(element, 10);
                    if (
                        method != null
                        && isNamespaceParam(method, element)
                        && ClassUtils.isCorrectRelatedClass(method, element.getProject(), ROUTE_NAMESPACES)
                    )
                    {
                        for (String namespace : getAllNamespaces(completionResultSet, element)) {
                            completionResultSet.addElement(
                                PsiElementUtils.buildSimpleLookupElement(namespace)
                            );
                        }
                    }
                }
            }
        );
    }

    private static @NotNull Set<String> getAllNamespaces(@NotNull CompletionResultSet completionResultSet, PsiElement element) {
        Set<String> namespaces = new HashSet<>();
        PhpIndex.getInstance(element.getProject()).getAllClassFqns(completionResultSet.getPrefixMatcher()).forEach(fqn -> {
            if (!fqn.isEmpty()) {
                int lastBackslashIndex = fqn.lastIndexOf("\\");

                if (lastBackslashIndex > 0) {
                    String namespace = StrUtils.addSlashes(
                        fqn.substring(0, lastBackslashIndex),
                        true,
                        true
                    );

                    namespaces.add(
                        namespace.replace("/", "\\")
                    );
                }
            }
        });
        return namespaces;
    }

    /**
     * Check if the given reference and position match the namespace parameter criteria
     * @param reference The method
     * @param position The PSI element position
     * @return True or false
     */
    private boolean isNamespaceParam(MethodReference reference, PsiElement position) {
        Integer expectedParamIndex = NAMESPACE_METHODS.get(reference.getName());

        if (expectedParamIndex == null) {
            return false;
        }

        return MethodUtils.findParamIndex(position, false) == expectedParamIndex;
    }
}

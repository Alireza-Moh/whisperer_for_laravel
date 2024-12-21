package at.alirezamoh.whisperer_for_laravel.routing.namespace;

import at.alirezamoh.whisperer_for_laravel.support.laravelUtils.ClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.psiUtil.PsiUtil;
import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
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

                    PsiElement parent = element.getParent().getParent().getParent();
                    if (
                        parent instanceof MethodReference methodReference
                        && Objects.equals(methodReference.getName(), "namespace")
                        && ClassUtils.isCorrectRelatedClass(methodReference, element.getProject(), ROUTE_NAMESPACES)
                    )
                    {
                        for (String namespace : getAllNamespaces(completionResultSet, element)) {
                            completionResultSet.addElement(
                                PsiUtil.buildSimpleLookupElement(namespace)
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
                    String namespace = StrUtil.addSlashes(
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
}

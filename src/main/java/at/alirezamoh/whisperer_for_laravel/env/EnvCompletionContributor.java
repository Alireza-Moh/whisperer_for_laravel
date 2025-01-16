package at.alirezamoh.whisperer_for_laravel.env;

import at.alirezamoh.whisperer_for_laravel.support.WhispererForLaravelIcon;
import at.alirezamoh.whisperer_for_laravel.support.utils.MethodUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.PluginUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Contributes completion suggestions for env values
 */
public class EnvCompletionContributor extends CompletionContributor {
    /**
     * The names of the methods env file getter
     */
    public static Map<String, Integer> ENV_FUNCTIONS = new HashMap<>() {{
        put("env", 0);
    }};

    /**
     * The names of the methods class
     */
    public static Map<String, Integer> ENV_CLASS_METHODS = new HashMap<>() {{
        put("get", 0);
        put("getOrFail", 0);
    }};

    /**
     * The FQN of the 'Env' class
     */
    private final String ENV = "\\Illuminate\\Support\\Env";

    EnvCompletionContributor() {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.or(
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL),
                PlatformPatterns.psiElement(PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE)
            ),
            new CompletionProvider<>() {

                @Override
                protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                    PsiElement psiElement = completionParameters.getPosition().getOriginalElement().getParent();
                    Project project = psiElement.getProject();

                    if (!PluginUtils.isLaravelProject(project) && PluginUtils.isLaravelFrameworkNotInstalled(project)) {
                        return;
                    }

                    if (isInsideEnvMethods(psiElement, project)) {
                        createLookUpElement(completionResultSet, project);
                    }
                }
            }
        );
    }

    /**
     * Creates completion env values and adds them to the result set
     *
     * @param result The completion result set
     */
    private void createLookUpElement(@NotNull CompletionResultSet result, Project project) {
        Map<String, String> envMap = EnvFileParser.parseEnvFile(project);

        for (Map.Entry<String, String> entry : envMap.entrySet()) {
            String key = entry.getKey();

            result.addElement(
                LookupElementBuilder
                    .create(key)
                    .withLookupString(key)
                    .withPresentableText(key)
                    .withTailText(" = " + entry.getValue(), true)
                    .bold()
                    .withIcon(WhispererForLaravelIcon.LARAVEL_ICON)
            );
        }
    }

    /**
     * Checks if the given PSI element is inside a method or function that can reference env value
     * @param psiElement The PSI element to check
     * @return True or false
     */
    private boolean isInsideEnvMethods(@NotNull PsiElement psiElement, Project project) {
        MethodReference method = MethodUtils.resolveMethodReference(psiElement, 10);
        if (method != null) {
            return isEnvMethodParam(method, psiElement) && isEnvClassMethod(method, project);
        }

        FunctionReference function = MethodUtils.resolveFunctionReference(psiElement, 10);
        return function != null && isEnvFunctionParam(function, psiElement);
    }

    /**
     * Checks if the given method reference is an env method
     * @param methodReference The method reference
     * @param project The project context
     * @return True or false
     */
    private boolean isEnvClassMethod(MethodReference methodReference, Project project) {
        String methodName = methodReference.getName();
        List<PhpClassImpl> resolvedClasses = MethodUtils.resolveMethodClasses(methodReference, project);

        PhpClass envClass = PhpClassUtils.getClassByFQN(project, ENV);

        return isExpectedFacadeMethod(methodName, resolvedClasses, envClass, ENV_CLASS_METHODS);
    }

    /**
     * Checks if a method matches the expected facade class and method map.
     *
     * @param methodName The method name.
     * @param resolvedClasses The list of resolved classes for the method.
     * @param expectedClass The expected facade class.
     * @param methodMap The map containing method names and expected parameter indices.
     * @return true or false
     */
    private boolean isExpectedFacadeMethod(String methodName, List<PhpClassImpl> resolvedClasses, PhpClass expectedClass, Map<String, Integer> methodMap) {
        return methodMap.containsKey(methodName)
            && expectedClass != null
            && resolvedClasses.stream().anyMatch(clazz -> PhpClassUtils.isChildOf(clazz, expectedClass));
    }

    /**
     * Checks if a method reference matches a parameter in the Env methods
     *
     * @param methodReference The method reference.
     * @param position The PSI element position.
     * @return true or false
     */
    private boolean isEnvMethodParam(MethodReference methodReference, PsiElement position) {
        return isExpectedParam(position, methodReference.getName(), ENV_CLASS_METHODS);
    }

    /**
     * Checks if a function reference matches a parameter in the env methods
     *
     * @param functionReference The function reference.
     * @param position The PSI element position.
     * @return true or false
     */
    private boolean isEnvFunctionParam(FunctionReference functionReference, PsiElement position) {
        return isExpectedParam(position, functionReference.getName(), ENV_FUNCTIONS);
    }

    /**
     * Check if a parameter matches the expected method and map.
     *
     * @param position The PSI element position.
     * @param methodName The method name.
     * @param methodMap The map containing method names and expected parameter indices.
     * @return true or false
     */
    private boolean isExpectedParam(PsiElement position, String methodName, Map<String, Integer> methodMap) {
        Integer expectedParamIndex = methodMap.get(methodName);
        return expectedParamIndex != null && expectedParamIndex == MethodUtils.findParamIndex(position, false);
    }
}

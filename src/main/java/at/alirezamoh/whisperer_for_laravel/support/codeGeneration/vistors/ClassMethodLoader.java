package at.alirezamoh.whisperer_for_laravel.support.codeGeneration.vistors;

import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Method;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocParamTagImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocParamTag;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClassMethodLoader {
    private Project project;

    public ClassMethodLoader(Project project) {
        this.project = project;
    }

    public List<Method> loadMethods(PsiFile file, String... methodsToBeIgnores) {
        Set<String> ignoreSet = methodsToBeIgnores != null ? Set.of(methodsToBeIgnores) : Collections.emptySet();

        List<Method> methods = new ArrayList<>();

        if (file instanceof PhpFile phpFile) {
            for (PhpClass phpClass : PhpClassUtils.getPhpClassesFromFile(phpFile)) {
                for (com.jetbrains.php.lang.psi.elements.Method actualMethod : PhpClassUtils.getClassPublicMethods(phpClass, true)) {
                    Method method = extractMethodInfo(actualMethod, ignoreSet);
                    if (method != null) {
                        methods.add(method);
                    }
                }
            }
        }
        return methods;
    }

    public @Nullable Method extractMethodInfo(com.jetbrains.php.lang.psi.elements.Method actualMethod, Set<String> methodsTobeIgnored) {
        String methodName = actualMethod.getName();
        if (methodName == null) {
            return null;
        }

        if (methodsTobeIgnored.contains(methodName)) {
            return null;
        }

        Method method = new Method(methodName);
        method.setSee(
            Objects.requireNonNull(actualMethod.getContainingClass()).getFQN()
        );
        this.getMethodInfo(method, actualMethod);

        return method;
    }

    public void getMethodInfo(Method method, com.jetbrains.php.lang.psi.elements.Method actualMethod) {
        List<String> phpTypes = Arrays.asList(
            "int",
            "float",
            "bool",
            "string",
            "array",
            "object",
            "mixed",
            "null",
            "never"
        );

        method.setReturnType(
            actualMethod.getType().global(project).toString()
        );

        for (Parameter param : actualMethod.getParameters()) {
            StringBuilder tempType = new StringBuilder();
            PhpDocComment phpDocComment = param.getDocComment();

            if (phpDocComment != null) {
                for (PhpDocParamTag phpDocParamTag : phpDocComment.getParamTags()) {
                    if (
                        phpDocParamTag instanceof PhpDocParamTagImpl paramTag
                            && (
                            (
                                paramTag.getVarName() != null && paramTag.getVarName().equals(param.getName())
                            )
                                || paramTag.getText().endsWith("$" + param.getName())
                        )
                    ) {
                        List<String> types = splitComplexType(phpDocParamTag.getText());
                        for (String type : types) {
                            String[] parts = type.split("[\\s,<>|]+");

                            for (String part : parts) {
                                String cleanedPart = StrUtils.removePhpExtension(part);
                                if (phpTypes.contains(cleanedPart) && !tempType.toString().contains(cleanedPart)) {
                                    getParamType(part, tempType);
                                }
                            }
                        }
                        for (PsiElement child : paramTag.getChildren()) {
                            if (child instanceof PhpDocTypeImpl phpDocType) {
                                String globalType = phpDocType.getGlobalType().toString();
                                if (!tempType.toString().contains(globalType)) {
                                    getParamType(globalType, tempType);
                                }
                            }
                        }
                    }
                }
            }

            PsiElement defaultValue = param.getDefaultValue();

            at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Parameter p =
                new at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Parameter();
            p.setName(param.getName());

            p.setType(tempType.toString());

            if (defaultValue != null) {
                p.setDefaultValue(defaultValue.getText());
            }
            else {
                p.setDefaultValue("");
            }
            method.addParameter(p);
        }
    }

    private void getParamType(String text, StringBuilder tempType) {
        if (tempType.isEmpty()) {
            tempType.append(text);
        }
        else {
            tempType.append("|").append(text);
        }
    }

    private List<String> splitComplexType(String typeString) {
        return Arrays.asList(typeString.split("\\|"));
    }
}

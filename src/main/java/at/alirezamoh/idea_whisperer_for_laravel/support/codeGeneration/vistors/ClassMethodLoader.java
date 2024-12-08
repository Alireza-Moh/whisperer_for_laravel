package at.alirezamoh.idea_whisperer_for_laravel.support.codeGeneration.vistors;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables.Method;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocTypeImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocParamTagImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocParamTag;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ClassMethodLoader {
    private Project project;

    public ClassMethodLoader(Project project) {
        this.project = project;
    }

    public List<Method> loadMethods(PsiFile file) {
        List<Method> methods = new ArrayList<>();

        if (file instanceof PhpFile phpFile) {
            phpFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
               @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof PhpClass phpClass) {
                        for (com.jetbrains.php.lang.psi.elements.Method actualMethod : phpClass.getOwnMethods()) {
                            Method method = extractMethodInfo(actualMethod);
                            if (method.getName() != null) {
                                methods.add(method);
                            }
                        }
                        return;
                    }
                    super.visitElement(element);
                }
            });
        }
        return methods;
    }

    public List<Method> loadMethodsWithIgnore(PsiFile file, List<String> ignoreList) {
        List<Method> methods = new ArrayList<>();

        if (file instanceof PhpFile phpFile) {
            phpFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof PhpClass phpClass) {
                        for (com.jetbrains.php.lang.psi.elements.Method actualMethod : phpClass.getOwnMethods()) {
                            Method method = extractMethodInfo(actualMethod, ignoreList);
                            if (method != null && method.getName() != null) {
                                methods.add(method);
                            }
                        }
                        return;
                    }
                    super.visitElement(element);
                }
            });
        }
        return methods;
    }

    public @Nullable Method extractMethodInfo(com.jetbrains.php.lang.psi.elements.Method actualMethod, List<String> ignoreList) {
        if (ignoreList.contains(actualMethod.getName())) {
            return null;
        }

        Method method = new Method();
        if (actualMethod.getAccess().isPublic()) {
            method.setName(actualMethod.getName());
            method.setSee(
                Objects.requireNonNull(actualMethod.getContainingClass()).getFQN()
            );
            this.getMethodInfo(method, actualMethod);
        }
        return method;
    }

    public Method extractMethodInfo(com.jetbrains.php.lang.psi.elements.Method actualMethod) {
        Method method = new Method();
        if (actualMethod.getAccess().isPublic()) {
            method.setName(actualMethod.getName());
            method.setSee(
                    Objects.requireNonNull(actualMethod.getContainingClass()).getFQN()
            );
            this.getMethodInfo(method, actualMethod);
        }
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
                                String cleanedPart = StrUtil.removeExtension(part);
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

            at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables.Parameter p =
                new at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables.Parameter();
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

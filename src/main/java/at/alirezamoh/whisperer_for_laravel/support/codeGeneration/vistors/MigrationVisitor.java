package at.alirezamoh.whisperer_for_laravel.support.codeGeneration.vistors;

import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.RenameField;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Table;
import at.alirezamoh.whisperer_for_laravel.support.codeGeneration.PhpTypeConverter;
import at.alirezamoh.whisperer_for_laravel.support.utils.PhpClassUtils;
import at.alirezamoh.whisperer_for_laravel.support.utils.StrUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class MigrationVisitor extends PsiRecursiveElementWalkingVisitor {

    private final List<String> AVAILABLE_TYPES = new ArrayList<>(List.of(
        "bigIncrements", "bigInteger", "binary", "boolean", "char", "dateTimeTz", "dateTime", "date",
        "decimal", "double", "enum", "float", "foreignId", "foreignIdFor", "foreignUlid", "foreignUuid",
        "geography", "geometry", "id", "increments", "integer", "ipAddress", "json", "jsonb", "longText",
        "macAddress", "mediumIncrements", "mediumInteger", "mediumText", "morphs", "nullableMorphs",
        "nullableTimestamps", "nullableUlidMorphs", "nullableUuidMorphs", "rememberToken", "set",
        "smallIncrements", "smallInteger", "softDeletesTz", "softDeletes", "string", "text", "timeTz",
        "time", "timestampTz", "timestamp", "timestampsTz", "timestamps", "tinyIncrements", "tinyInteger",
        "tinyText", "unsignedBigInteger", "unsignedInteger", "unsignedMediumInteger", "unsignedSmallInteger",
        "unsignedTinyInteger", "ulidMorphs", "uuidMorphs", "ulid", "uuid", "year",
        "renameColumn", "dropColumn", "dropMorphs", "dropRememberToken",
        "dropSoftDeletes", "dropSoftDeletesTz", "dropTimestamps", "dropTimestampsTz",
        "dropPrimary", "dropUnique", "dropIndex", "dropFullText", "dropSpatialIndex", "dropForeign"
    ));

    private final List<String> AVOIDABLE_TYPES = new ArrayList<>(List.of(
        "dropColumn", "dropMorphs", "dropRememberToken",
        "dropSoftDeletes", "dropSoftDeletesTz", "dropTimestamps", "dropTimestampsTz",
        "dropPrimary", "dropUnique", "dropIndex", "dropFullText", "dropSpatialIndex", "dropForeign"
    ));

    private final String[] SCHEMA_NAMESPACES = {"\\Illuminate\\Support\\Facades\\Schema", "\\Schema"};


    private final List<Table> tables = new ArrayList<>();

    private String currentTableName = null;

    @Override
    public void visitElement(@NotNull PsiElement element) {
        if (shouldScanMethod(element)) {
            extractTableName((MethodReferenceImpl) element);
        }
        super.visitElement(element);
    }

    public List<Table> getTables() {
        return tables;
    }

    private boolean shouldScanMethod(PsiElement element) {
        return element instanceof MethodReferenceImpl methodReference
            && isInsideUpMethod(methodReference)
            && isCreateOrTable(methodReference)
            && PhpClassUtils.isCorrectRelatedClass(methodReference, methodReference.getProject(), SCHEMA_NAMESPACES);
    }

    private void extractTableName(MethodReferenceImpl methodReference) {
        PsiElement parameterTableName = methodReference.getParameter(0);

        if (parameterTableName instanceof StringLiteralExpression) {
            String tableName = StrUtils.removeQuotes(parameterTableName.getText());

            if (tableName.isEmpty()) {
                return;
            }

            boolean tableExists = tables.stream().anyMatch(table -> table.name().equals(tableName));
            currentTableName = tableName;

            if (!tableExists) {
                tables.add(new Table(currentTableName, methodReference, new ArrayList<>()));
            }

            getFieldsForTable(methodReference);
        }
    }

    private void getFieldsForTable(MethodReference methodReference) {
        List<MethodReference> methodReferences = getColumnsMethodsForTable(methodReference);

        for (MethodReference statementMethodReference : methodReferences) {
            processMethodReference(statementMethodReference);
        }
    }

    private void processMethodReference(MethodReference methodReference) {
        if (isColumnDefinition(methodReference)) {
            addColumn(methodReference, isNullable(methodReference));
        }

        for (PsiElement childElement : methodReference.getChildren()) {
            if (childElement instanceof MethodReference childMethodReference) {
                processMethodReference(childMethodReference);
            }
        }
    }

    private boolean isInsideUpMethod(MethodReference methodReference) {
        Method parentMethod = PsiTreeUtil.getParentOfType(methodReference, Method.class);
        return parentMethod != null && "up".equals(parentMethod.getName());
    }

    private boolean isCreateOrTable(MethodReference methodReference) {
        String methodName = methodReference.getName();
        return "create".equals(methodName) || "table".equals(methodName);
    }

    private boolean isColumnDefinition(MethodReference method) {
        String methodName = method.getName();

        return methodName != null && AVAILABLE_TYPES.contains(methodName);
    }

    private List<MethodReference> getColumnsMethodsForTable(MethodReference method) {
        return Arrays.stream(method.getParameters())
            .filter(parameter -> parameter instanceof PhpExpression)
            .flatMap(parameter -> Arrays.stream(parameter.getChildren()))
            .filter(child -> child instanceof FunctionImpl)
            .flatMap(function -> Arrays.stream(function.getChildren()))
            .filter(child -> child instanceof GroupStatement)
            .flatMap(groupStatement -> Arrays.stream(groupStatement.getChildren()))
            .filter(statement -> statement instanceof Statement)
            .map(PsiElement::getFirstChild)
            .filter(child -> child instanceof MethodReference)
            .map(child -> (MethodReference) child)
            .filter(methodReference -> !isCreateOrTable(methodReference))
            .collect(Collectors.toList());
    }

    private void addColumn(MethodReference referenceMethod, boolean nullable) {
        Table targetTable = tables.stream()
            .filter(table -> currentTableName.equals(table.name()))
            .findFirst()
            .orElse(null);

        if (targetTable != null) {
            if (isId(referenceMethod) && doesNotContain(targetTable.fields(), "id") && referenceMethod.getName() != null) {
                targetTable.fields().add(new Field(PhpTypeConverter.convert(referenceMethod.getName()), "id", false, false, false, null));
            } else if (isTimestamps(referenceMethod)) {
                if (doesNotContain(targetTable.fields(), "created_at")) {
                    targetTable.fields().add(new Field("Carbon", "created_at", true, false, false, null));
                }

                if (doesNotContain(targetTable.fields(), "updated_at")) {
                    targetTable.fields().add(new Field("Carbon", "updated_at", true, false, false, null));
                }
            } else if (isSoftDeletes(referenceMethod) && doesNotContain(targetTable.fields(), "deleted_at")) {
                targetTable.fields().add(new Field("Carbon", "deleted_at", true, false, false, null));
            } else if (isColumnDefinition(referenceMethod)) {
                List<Field> names = getColumnName(referenceMethod);

                for (Field field : names) {
                    field.setNullable(nullable);
                    targetTable.fields().add(field);
                }
            }
        }
    }

    private boolean doesNotContain(List<Field> fields, String name) {
        return fields.stream().noneMatch(field -> field.getName().equals(name));
    }

    private static boolean isId(MethodReference methodReference) {
        return "id".equals(methodReference.getName());
    }

    private static boolean isTimestamps(MethodReference methodReference) {
        String methodName = methodReference.getName();
        return "timestamps".equals(methodName) || "timestampsTz".equals(methodName);
    }

    private static boolean isSoftDeletes(MethodReference methodReference) {
        String methodName = methodReference.getName();
        return "softDeletes".equals(methodName) || "softDeletesTz".equals(methodName);
    }

    private List<Field> getColumnName(MethodReference method) {
        List<Field> fields = new ArrayList<>();
        String methodName = method.getName();

        if (methodName == null) {
            return fields;
        }

        PsiElement parameter = method.getParameter(0);
        boolean isRenameDefinition = Objects.equals(methodName, "renameColumn");
        RenameField renameField = null;

        PsiElement secParameter = method.getParameter(1);

        if (isRenameDefinition && parameter instanceof StringLiteralExpression && secParameter instanceof StringLiteralExpression) {
            renameField = new RenameField(
                StrUtils.removeQuotes(parameter.getText()),
                StrUtils.removeQuotes(secParameter.getText())
            );
        }

        if (parameter instanceof ArrayCreationExpressionImpl array) {
            for (PsiElement value : array.getChildren()) {
                if (value instanceof PhpPsiElement phpPsiElement) {
                    fields.add(
                        new Field(
                            PhpTypeConverter.convert(methodName),
                            StrUtils.removeQuotes(phpPsiElement.getText()),
                            false,
                            AVOIDABLE_TYPES.contains(methodName),
                            isRenameDefinition,
                            renameField
                        )
                    );
                }
            }
        } else if (parameter instanceof StringLiteralExpression) {
            fields.add(
                new Field(
                    PhpTypeConverter.convert(methodName),
                    StrUtils.removeQuotes(parameter.getText()),
                    false,
                    AVOIDABLE_TYPES.contains(methodName),
                    isRenameDefinition,
                    renameField
                )
            );
        }

        return fields;
    }

    private boolean isNullable(MethodReference methodReference) {
        PsiElement nextSibling = methodReference.getNextSibling();

        if (
            nextSibling instanceof LeafPsiElement
                && nextSibling.textMatches("->")
                && nextSiblingInTreeWithText(nextSibling) != null
        ) {

            PsiElement nullableSibling = nextSiblingInTreeWithText(nextSibling);

            if (nullableSibling != null && nullableSibling.getNextSibling() != null &&
                nullableSibling.getNextSibling().getNextSibling() instanceof ParameterList parameterList) {

                PsiElement parameter = parameterList.getParameter(0);

                if (parameter instanceof ConstantReference constantReference) {
                    String canonicalText = constantReference.getCanonicalText();
                    return "true".equals(canonicalText);
                }
                else {
                    return true;
                }
            }
        }

        return false;
    }

    private PsiElement nextSiblingInTreeWithText(PsiElement element) {
        PsiElement current = element.getNextSibling();

        while (current != null) {
            if (current instanceof LeafPsiElement leafPsiElement&& leafPsiElement.textMatches("nullable")) {
                return current;
            }
            current = current.getNextSibling();
        }

        return null;
    }
}

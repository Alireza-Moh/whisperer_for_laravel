package at.alirezamoh.idea_whisperer_for_laravel.support.codeGeneration.vistors;

import at.alirezamoh.idea_whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.idea_whisperer_for_laravel.support.codeGeneration.PhpTypeConverter;
import at.alirezamoh.idea_whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.ArrayCreationExpressionImpl;
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl;
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MigrationVisitor extends PsiRecursiveElementWalkingVisitor {
    private final String BLUE_PRINT_CLASS_NAMESPACE = "Illuminate\\Database\\Schema\\Blueprint";

    private final String SCHEMA_CLASS_NAMESPACE = "\\Illuminate\\Support\\Facades\\Schema";

    private final List<String> AVAILABLE_TYPES = new ArrayList<>(List.of(
        "bigIncrements", "bigInteger", "binary", "boolean", "char", "dateTimeTz", "dateTime", "date",
        "decimal", "double", "enum", "float", "foreignId", "foreignIdFor", "foreignUlid", "foreignUuid",
        "geography", "geometry", "id", "increments", "integer", "ipAddress", "json", "jsonb", "longText",
        "macAddress", "mediumIncrements", "mediumInteger", "mediumText", "morphs", "nullableMorphs",
        "nullableTimestamps", "nullableUlidMorphs", "nullableUuidMorphs", "rememberToken", "set",
        "smallIncrements", "smallInteger", "softDeletesTz", "softDeletes", "string", "text", "timeTz",
        "time", "timestampTz", "timestamp", "timestampsTz", "timestamps", "tinyIncrements", "tinyInteger",
        "tinyText", "unsignedBigInteger", "unsignedInteger", "unsignedMediumInteger", "unsignedSmallInteger",
        "unsignedTinyInteger", "ulidMorphs", "uuidMorphs", "ulid", "uuid", "year"
    ));

    private final List<String> AVOIDABLE_TYPES = new ArrayList<>(List.of(
        "renameColumn", "dropColumn", "dropMorphs", "dropRememberToken",
        "dropSoftDeletes", "dropSoftDeletesTz", "dropTimestamps", "dropTimestampsTz",
        "dropPrimary", "dropUnique", "dropIndex", "dropFullText", "dropSpatialIndex", "dropForeign"
    ));


    private final Map<String, List<Field>> tables = new HashMap<>();

    private String currentTableName = null;

    @Override
    public void visitElement(@NotNull PsiElement element) {
        if (element instanceof StringLiteralExpression stringLiteralExpression) {
            extractTableName(stringLiteralExpression);
        }

        if (element instanceof MethodReferenceImpl methodReference && currentTableName != null) {
            getField(methodReference);
        }
        super.visitElement(element);
    }

    public Map<String, List<Field>> getTables() {
        return tables;
    }

    private void extractTableName(StringLiteralExpression stringLiteralExpression) {
        MethodReferenceImpl method = PsiTreeUtil.getParentOfType(stringLiteralExpression, MethodReferenceImpl.class);

        if (method != null && (Objects.equals(method.getName(), "create") || Objects.equals(method.getName(), "table"))) {

            PsiElement potentialClass = method.getFirstChild();
            if (potentialClass instanceof ClassReferenceImpl classReference && Objects.equals(classReference.getFQN(), SCHEMA_CLASS_NAMESPACE)) {
                currentTableName = StrUtil.removeQuotes(stringLiteralExpression.getText());
                tables.putIfAbsent(currentTableName, new ArrayList<>());
            }
        }
    }

    private void getField(MethodReferenceImpl methodReference) {
        PsiReference reference = methodReference.getReference();

        if (reference != null) {
            PsiElement resolvedReference = reference.resolve();
            PsiElement parameter = methodReference.getParameter(0);
            String fieldName = parameter != null ? StrUtil.removeQuotes(parameter.getText()) : "";
            extractFieldType(resolvedReference, fieldName, parameter);
        }
    }

    private void extractFieldType(PsiElement resolvedReference, String fieldName, PsiElement parameter) {
        if (resolvedReference instanceof MethodImpl method) {
            PhpClass bluePrintClass = method.getContainingClass();
            if (bluePrintClass != null && bluePrintClass.getPresentableFQN().equals(BLUE_PRINT_CLASS_NAMESPACE)) {
                if (AVAILABLE_TYPES.contains(method.getName()) && !AVOIDABLE_TYPES.contains(method.getName())) {
                    addNewFieldToTable(fieldName, method, false);
                }
                else {
                    if (parameter instanceof ArrayCreationExpressionImpl array) {
                        PsiElement[] values = array.getChildren();

                        for (PsiElement value : values) {
                            if (value instanceof PhpPsiElement phpPsiElement) {
                                addNewFieldToTable(StrUtil.removeQuotes(phpPsiElement.getText()), method, true);
                            }
                        }
                    } else if (parameter instanceof StringLiteralExpression) {
                        addNewFieldToTable(fieldName, method, true);
                    }
                }
            }

        }
    }

    private void addNewFieldToTable(String fieldName, MethodImpl method, boolean drop) {
        List<Field> fields = tables.get(currentTableName);
        if (fields != null && fields.stream().noneMatch(field -> field.getName().equals(fieldName))) {
            String type = method.getName();
            switch (type) {
                case "id" -> fields.add(new Field(PhpTypeConverter.convert(type), "id", false, drop));
                case "timestamps" -> {
                    fields.add(new Field(PhpTypeConverter.convert(type), "created_at", true, drop));
                    fields.add(new Field(PhpTypeConverter.convert(type), "updated_at", true, drop));
                }
                default -> fields.add(new Field(PhpTypeConverter.convert(type), fieldName, false, drop));
            }
        }
    }

    private void deleteDroppedColumns(PsiElement parameter) {
        List<Field> fields = tables.get(currentTableName);
        if (fields != null) {
            if (parameter instanceof ArrayCreationExpressionImpl array) {
                PsiElement[] values = array.getChildren();
                List<String> realValues = new ArrayList<>();

                for (PsiElement value : values) {
                    if (value instanceof PhpPsiElement phpPsiElement) {
                        realValues.add(StrUtil.removeQuotes(phpPsiElement.getText()));
                    }
                }

                for (String realValue : realValues) {
                    fields.removeIf(field -> field.getName().equals(realValue));
                }
            } else if (parameter instanceof StringLiteralExpression value) {
                fields.removeIf(field -> field.getName().equals(value.getText()));
            }
        }
    }
}

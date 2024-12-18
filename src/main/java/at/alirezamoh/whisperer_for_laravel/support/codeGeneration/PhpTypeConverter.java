package at.alirezamoh.whisperer_for_laravel.support.codeGeneration;

import java.util.HashMap;
import java.util.Map;

public class PhpTypeConverter {
    private static final Map<String, String> typeMap = new HashMap<>();

    static {
        typeMap.put("bigIncrements", "int");
        typeMap.put("bigInteger", "int");
        typeMap.put("binary", "string");
        typeMap.put("boolean", "bool");
        typeMap.put("char", "string");
        typeMap.put("date", "Carbon");
        typeMap.put("dateTime", "Carbon");
        typeMap.put("dateTimeTz", "Carbon");
        typeMap.put("decimal", "float");
        typeMap.put("double", "float");
        typeMap.put("enum", "string");
        typeMap.put("float", "float");
        typeMap.put("foreignId", "int");
        typeMap.put("foreignUlid", "string");
        typeMap.put("foreignUuid", "string");
        typeMap.put("geometry", "object");
        typeMap.put("geography", "object");
        typeMap.put("id", "int");
        typeMap.put("increments", "int");
        typeMap.put("integer", "int");
        typeMap.put("ipAddress", "string");
        typeMap.put("json", "array");
        typeMap.put("jsonb", "array");
        typeMap.put("longText", "string");
        typeMap.put("macAddress", "string");
        typeMap.put("mediumIncrements", "int");
        typeMap.put("mediumInteger", "int");
        typeMap.put("mediumText", "string");
        typeMap.put("morphs_id", "int");
        typeMap.put("morphs_type", "string");
        typeMap.put("nullableMorphs_id", "int|null");
        typeMap.put("nullableMorphs_type", "string|null");
        typeMap.put("created_at", "Carbon|null");
        typeMap.put("updated_at", "Carbon|null");
        typeMap.put("nullableUlidMorphs_id", "string|null");
        typeMap.put("nullableUlidMorphs_type", "string|null");
        typeMap.put("nullableUuidMorphs_id", "string|null");
        typeMap.put("nullableUuidMorphs_type", "string|null");
        typeMap.put("rememberToken", "string|null");
        typeMap.put("set", "object");
        typeMap.put("smallIncrements", "int");
        typeMap.put("smallInteger", "int");
        typeMap.put("deleted_at", "Carbon|null");
        typeMap.put("string", "string");
        typeMap.put("text", "string");
        typeMap.put("time", "Carbon");
        typeMap.put("timeTz", "Carbon");
        typeMap.put("timestamps", "Carbon");
        typeMap.put("timestamp", "Carbon");
        typeMap.put("timestampTz", "Carbon");
        typeMap.put("tinyIncrements", "int");
        typeMap.put("tinyInteger", "int");
        typeMap.put("tinyText", "string");
        typeMap.put("unsignedBigInteger", "int");
        typeMap.put("unsignedInteger", "int");
        typeMap.put("unsignedMediumInteger", "int");
        typeMap.put("unsignedSmallInteger", "int");
        typeMap.put("unsignedTinyInteger", "int");
        typeMap.put("ulidMorphs_id", "string");
        typeMap.put("ulidMorphs_type", "string");
        typeMap.put("uuid", "string");
        typeMap.put("uuidMorphs_id", "string");
        typeMap.put("uuidMorphs_type", "string");
        typeMap.put("year", "int");
    }

    public static String convert(String mysqlType) {
        return typeMap.getOrDefault(mysqlType, "mixed");
    }
}

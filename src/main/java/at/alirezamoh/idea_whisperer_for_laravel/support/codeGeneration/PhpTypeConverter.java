package at.alirezamoh.idea_whisperer_for_laravel.support.codeGeneration;

import java.util.HashMap;
import java.util.Map;

public class PhpTypeConverter {
    private static final Map<String, String> typeMap = new HashMap<>();

    static {
        // Basic types
        typeMap.put("bigint", "int");
        typeMap.put("bigint unsigned", "int");
        typeMap.put("blob", "string");
        typeMap.put("varbinary", "string");
        typeMap.put("binary", "string");
        typeMap.put("tinyint(1)", "bool");
        typeMap.put("char", "string");
        typeMap.put("datetime", "Carbon");
        typeMap.put("date", "Carbon");
        typeMap.put("decimal", "float");
        typeMap.put("double", "float");
        typeMap.put("enum", "string");
        typeMap.put("float", "float");
        typeMap.put("int", "int");
        typeMap.put("int unsigned", "int");
        typeMap.put("json", "array");
        typeMap.put("longtext", "string");
        typeMap.put("longblob", "string");
        typeMap.put("varchar", "string");
        typeMap.put("mediumint", "int");
        typeMap.put("mediumint unsigned", "int");
        typeMap.put("mediumtext", "string");
        typeMap.put("mediumblob", "string");
        typeMap.put("smallint", "int");
        typeMap.put("smallint unsigned", "int");
        typeMap.put("text", "string");
        typeMap.put("time", "Carbon");
        typeMap.put("timestamp", "Carbon");
        typeMap.put("tinyint", "int");
        typeMap.put("tinyint unsigned", "int");
        typeMap.put("tinytext", "string");
        typeMap.put("tinyblob", "string");
        typeMap.put("year", "Carbon");

        // Special types
        typeMap.put("point", "object");
        typeMap.put("set('value1','value2')", "string");
        typeMap.put("char(26)", "string");
        typeMap.put("char(36)", "string");
    }

    public static String convert(String mysqlType) {
        String typeToLowerCase = mysqlType.toLowerCase();
        String phpType;

        if (typeToLowerCase.startsWith("varchar(")) {
            phpType = "string";
        }
        else {
            phpType = typeMap.getOrDefault(typeToLowerCase, "mixed");
        }

        return phpType;
    }
}

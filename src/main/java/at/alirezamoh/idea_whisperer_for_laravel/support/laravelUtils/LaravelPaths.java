package at.alirezamoh.idea_whisperer_for_laravel.support.laravelUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaravelPaths {
    public static class LaravelClasses {
        public static final String QueryBuilder = "\\Illuminate\\Database\\Query\\Builder";
        public static final String EloquentBuilder = "\\Illuminate\\Database\\Eloquent\\Builder";
        public static final String SchemaBuilder = "\\Illuminate\\Database\\Schema\\Builder";
        public static final String Blueprint = "\\Illuminate\\Database\\Schema\\Blueprint";
        public static final String JoinClause = "\\Illuminate\\Database\\Query\\JoinClause";
        public static final String Relation = "\\Illuminate\\Database\\Eloquent\\Relations\\Relation";
        public static final String Model = "\\Illuminate\\Database\\Eloquent\\Model";
        public static final String DbFacade = "\\Illuminate\\Support\\Facades\\DB";
        public static final String DbFacadeAlias = "\\DB";
        public static final String SchemaFacade = "\\Illuminate\\Support\\Facades\\Schema";
        public static final String SchemaFacadeAlias = "\\Schema";
        public static final String ColumnDefinition = "\\Illuminate\\Database\\Schema\\ColumnDefinition";
        public static final String TestCase = "\\Illuminate\\Foundation\\Testing\\TestCase";
        public static final String QueryRelation = "\\Illuminate\\Database\\Eloquent\\Concerns\\QueriesRelationships";
        public static final String Request = "\\Illuminate\\Http\\Request";
        public static final String FormRequest = "\\Illuminate\\Foundation\\Http\\FormRequest";
        public static final String Validator = "\\Illuminate\\Support\\Facades\\Validator";
        public static final String Gate = "\\Illuminate\\Support\\Facades\\Gate";
    }

    public static List<String> RELEVANT_LARAVEL_CLASSES = Arrays.asList(
        LaravelClasses.Model,
        LaravelClasses.QueryBuilder,
        LaravelClasses.EloquentBuilder,
        LaravelClasses.SchemaBuilder,
        LaravelClasses.Blueprint,
        LaravelClasses.JoinClause,
        LaravelClasses.Relation,
        LaravelClasses.DbFacade,
        LaravelClasses.DbFacadeAlias,
        LaravelClasses.SchemaFacade,
        LaravelClasses.SchemaFacadeAlias,
        LaravelClasses.ColumnDefinition,
        LaravelClasses.TestCase,
        LaravelClasses.QueryRelation,
        LaravelClasses.Validator,
        LaravelClasses.FormRequest,
        LaravelClasses.Request,
        LaravelClasses.Gate
    );

    public static Map<String, List<Integer>> DB_TABLE_METHODS = new HashMap<>() {{
        put("table", Arrays.asList(0));
        put("from", Arrays.asList(0));
        put("join", Arrays.asList(0));
        put("joinWhere", Arrays.asList(0));
        put("leftJoin", Arrays.asList(0));
        put("leftJoinWhere", Arrays.asList(0));
        put("rightJoin", Arrays.asList(0));
        put("rightJoinWhere", Arrays.asList(0));
        put("crossJoin", Arrays.asList(0));
        put("hasTable", Arrays.asList(0));
        put("getColumnListing", Arrays.asList(0));
        put("hasColumn", Arrays.asList(0));
        put("hasColumns", Arrays.asList(0));
        put("getColumnType", Arrays.asList(0));
        put("create", Arrays.asList(0));
        put("drop", Arrays.asList(0));
        put("dropIfExists", Arrays.asList(0));
        put("dropColumns", Arrays.asList(0));
        put("rename", Arrays.asList(0));
        put("assertDatabaseHas", Arrays.asList(0));
        put("assertDatabaseMissing", Arrays.asList(0));
        put("assertDatabaseCount", Arrays.asList(0));
        put("assertDeleted", Arrays.asList(0));
        put("assertSoftDeleted", Arrays.asList(0));
        put("fromSub", Arrays.asList(0));
        put("selectSub", Arrays.asList(0));
    }};

    public static Map<String, List<Integer>> BUILDER_METHODS = new HashMap<>() {{
        put("select", Arrays.asList(-1));
        put("addSelect", Arrays.asList(0));
        put("join", Arrays.asList(1, 2, 3));
        put("joinWhere", Arrays.asList(1));
        put("joinSub", Arrays.asList(2, 3, 4));
        put("leftJoin", Arrays.asList(1, 2, 3));
        put("leftJoinWhere", Arrays.asList(1));
        put("leftJoinSub", Arrays.asList(2, 3, 4));
        put("rightJoin", Arrays.asList(1, 2, 3));
        put("rightJoinWhere", Arrays.asList(1));
        put("rightJoinSub", Arrays.asList(2, 3, 4));
        put("crossJoin", Arrays.asList(1, 2, 3));
        put("where", Arrays.asList(0));
        put("whereNot", Arrays.asList(0));
        put("orWhere", Arrays.asList(0));
        put("orWhereNot", Arrays.asList(0));
        put("whereColumn", Arrays.asList(0, 1, 2));
        put("orWhereColumn", Arrays.asList(0, 1, 2));
        put("whereIn", Arrays.asList(0));
        put("orWhereIn", Arrays.asList(0));
        put("whereNotIn", Arrays.asList(0));
        put("orWhereNotIn", Arrays.asList(0));
        put("whereIntegerInRaw", Arrays.asList(0));
        put("orWhereIntegerInRaw", Arrays.asList(0));
        put("orWhereIntegerNotInRaw", Arrays.asList(0));
        put("whereNull", Arrays.asList(0));
        put("orWhereNull", Arrays.asList(0));
        put("whereNotNull", Arrays.asList(0));
        put("whereBetween", Arrays.asList(0));
        put("whereBetweenColumns", Arrays.asList(0, 1));
        put("orWhereBetween", Arrays.asList(0));
        put("orWhereBetweenColumns", Arrays.asList(0, 1));
        put("whereNotBetween", Arrays.asList(0));
        put("whereNotBetweenColumns", Arrays.asList(0, 1));
        put("orWhereNotBetween", Arrays.asList(0));
        put("orWhereNotBetweenColumns", Arrays.asList(0, 1));
        put("orWhereNotNull", Arrays.asList(0));
        put("whereDate", Arrays.asList(0));
        put("orWhereDate", Arrays.asList(0));
        put("whereTime", Arrays.asList(0));
        put("orWhereTime", Arrays.asList(0));
        put("whereDay", Arrays.asList(0));
        put("orWhereDay", Arrays.asList(0));
        put("whereMonth", Arrays.asList(0));
        put("orWhereMonth", Arrays.asList(0));
        put("whereYear", Arrays.asList(0));
        put("orWhereYear", Arrays.asList(0));
        put("whereRowValues", Arrays.asList(0));
        put("orWhereRowValues", Arrays.asList(0));
        put("whereJsonContains", Arrays.asList(0));
        put("orWhereJsonContains", Arrays.asList(0));
        put("whereJsonDoesntContain", Arrays.asList(0));
        put("orWhereJsonDoesntContain", Arrays.asList(0));
        put("whereJsonLength", Arrays.asList(0));
        put("orWhereJsonLength", Arrays.asList(0));
        put("groupBy", Arrays.asList(-1));
        put("having", Arrays.asList(0));
        put("orHaving", Arrays.asList(0));
        put("havingBetween", Arrays.asList(0));
        put("orderBy", Arrays.asList(0));
        put("orderByDesc", Arrays.asList(0));
        put("latest", Arrays.asList(0));
        put("oldest", Arrays.asList(0));
        put("forPageBeforeId", Arrays.asList(2));
        put("forPageAfterId", Arrays.asList(2));
        put("reorder", Arrays.asList(0));
        put("find", Arrays.asList(1));
        put("value", Arrays.asList(0));
        put("get", Arrays.asList(-1));
        put("paginate", Arrays.asList(1));
        put("simplePaginate", Arrays.asList(1));
        put("getCountForPagination", Arrays.asList(0));
        put("pluck", Arrays.asList(0, 1));
        put("implode", Arrays.asList(0));
        put("count", Arrays.asList(0));
        put("min", Arrays.asList(0));
        put("max", Arrays.asList(0));
        put("sum", Arrays.asList(0));
        put("avg", Arrays.asList(0));
        put("average", Arrays.asList(0));
        put("aggregate", Arrays.asList(1));
        put("numericAggregate", Arrays.asList(1));
        put("insertUsing", Arrays.asList(1));
        put("increment", Arrays.asList(0));
        put("decrement", Arrays.asList(0));
        put("updateOrInsert", Arrays.asList(0, 1));
        put("update", Arrays.asList(0));
        put("on", Arrays.asList(0, 1, 2));
        put("hasColumn", Arrays.asList(1));
        put("hasColumns", Arrays.asList(1));
        put("getColumnType", Arrays.asList(1));
        put("dropColumn", Arrays.asList(0));
        put("dropColumns", Arrays.asList(1));
        put("dropConstrainedForeignId", Arrays.asList(0));
        put("renameColumn", Arrays.asList(0));
        put("dropSoftDeletes", Arrays.asList(0));
        put("dropSoftDeletesTz", Arrays.asList(0));
        put("unique", Arrays.asList(0));
        put("index", Arrays.asList(0));
        put("spatialIndex", Arrays.asList(0));
        put("foreign", Arrays.asList(0));
        put("indexCommand", Arrays.asList(1));
        put("createIndexName", Arrays.asList(1));
        put("after", Arrays.asList(0));
        put("removeColumn", Arrays.asList(0));
        put("primary", Arrays.asList(0));
        put("dropIndex", Arrays.asList(0));
        put("dropUnique", Arrays.asList(0));
        put("dropPrimary", Arrays.asList(0));
        put("dropForeign", Arrays.asList(0));
        put("dropSpatialIndex", Arrays.asList(0));
        put("id", Arrays.asList(0));
        put("increments", Arrays.asList(0));
        put("integerIncrements", Arrays.asList(0));
        put("tinyIncrements", Arrays.asList(0));
        put("mediumIncrements", Arrays.asList(0));
        put("bigIncrements", Arrays.asList(0));
        put("char", Arrays.asList(0));
        put("string", Arrays.asList(0));
        put("text", Arrays.asList(0));
        put("mediumText", Arrays.asList(0));
        put("longText", Arrays.asList(0));
        put("integer", Arrays.asList(0));
        put("tinyInteger", Arrays.asList(0));
        put("smallInteger", Arrays.asList(0));
        put("mediumInteger", Arrays.asList(0));
        put("bigInteger", Arrays.asList(0));
        put("unsignedInteger", Arrays.asList(0));
        put("unsignedTinyInteger", Arrays.asList(0));
        put("unsignedSmallInteger", Arrays.asList(0));
        put("unsignedMediumInteger", Arrays.asList(0));
        put("unsignedBigInteger", Arrays.asList(0));
        put("foreignId", Arrays.asList(0));
        put("foreignIdFor", Arrays.asList(1));
        put("float", Arrays.asList(0));
        put("double", Arrays.asList(0));
        put("decimal", Arrays.asList(0));
        put("unsignedFloat", Arrays.asList(0));
        put("unsignedDouble", Arrays.asList(0));
        put("unsignedDecimal", Arrays.asList(0));
        put("boolean", Arrays.asList(0));
        put("enum", Arrays.asList(0));
        put("set", Arrays.asList(0));
        put("json", Arrays.asList(0));
        put("jsonb", Arrays.asList(0));
        put("date", Arrays.asList(0));
        put("dateTime", Arrays.asList(0));
        put("dateTimeTz", Arrays.asList(0));
        put("time", Arrays.asList(0));
        put("timeTz", Arrays.asList(0));
        put("timestamp", Arrays.asList(0));
        put("timestampTz", Arrays.asList(0));
        put("softDeletes", Arrays.asList(0));
        put("softDeletesTz", Arrays.asList(0));
        put("year", Arrays.asList(0));
        put("binary", Arrays.asList(0));
        put("uuid", Arrays.asList(0));
        put("foreignUuid", Arrays.asList(0));
        put("ipAddress", Arrays.asList(0));
        put("macAddress", Arrays.asList(0));
        put("geometry", Arrays.asList(0));
        put("point", Arrays.asList(0));
        put("lineString", Arrays.asList(0));
        put("polygon", Arrays.asList(0));
        put("geometryCollection", Arrays.asList(0));
        put("multiPoint", Arrays.asList(0));
        put("multiLineString", Arrays.asList(0));
        put("multiPolygon", Arrays.asList(0));
        put("multiPolygonZ", Arrays.asList(0));
        put("computed", Arrays.asList(0));
        put("create", Arrays.asList(0));
        put("update", Arrays.asList(0));
        put("fill", Arrays.asList(0));
        put("updateOrCreate", Arrays.asList(0, 1));
        put("updateOrInsert", Arrays.asList(0, 1));
        put("assertDatabaseHas", Arrays.asList(1));
        put("assertDatabaseMissing", Arrays.asList(1));
        put("assertDeleted", Arrays.asList(1));
        put("assertSoftDeleted", Arrays.asList(1));
    }};

    public static final Map<String, List<Integer>> QUERY_RELATION_PARAMS = new HashMap<>() {{
        put("with", List.of(0));
        put("has", List.of(0));
        put("hasNested", List.of(0));
        put("orHas", List.of(0));
        put("doesntHave", List.of(0));
        put("orDoesntHave", List.of(0));
        put("whereHas", List.of(0));
        put("withWhereHas", List.of(0));
        put("orWhereHas", List.of(0));
        put("whereDoesntHave", List.of(0));
        put("orWhereDoesntHave", List.of(0));
        put("hasMorph", List.of(0));
        put("orHasMorph", List.of(0));
        put("doesntHaveMorph", List.of(0));
        put("orDoesntHaveMorph", List.of(0));
        put("whereHasMorph", List.of(0));
        put("orWhereHasMorph", List.of(0));
        put("whereDoesntHaveMorph", List.of(0));
        put("orWhereDoesntHaveMorph", List.of(0));
        put("whereRelation", List.of(0, 1));
        put("orWhereRelation", List.of(0, 1));
        put("whereMorphRelation", List.of(0, 1, 2));
        put("orWhereMorphRelation", List.of(0, 1, 2));
        put("whereMorphedTo", List.of(0));
        put("orWhereMorphedTo", List.of(0));
        put("whereNotMorphedTo", List.of(0));
        put("orWhereNotMorphedTo", List.of(0));
        put("whereBelongsTo", List.of(1));
        put("orWhereBelongsTo", List.of(1));
        put("withAggregate", List.of(0, 1));
        put("withCount", List.of(0));
        put("withMax", List.of(0, 1));
        put("withMin", List.of(0, 1));
        put("withSum", List.of(0, 1));
        put("withAvg", List.of(0, 1));
        put("withExists", List.of(0));
    }};

}

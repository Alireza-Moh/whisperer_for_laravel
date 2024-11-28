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

    public static Map<String, Integer> DB_TABLE_METHODS = new HashMap<>() {{
        put("table", 0);
        put("from", 0);
        put("join", 0);
        put("joinWhere", 0);
        put("leftJoin", 0);
        put("leftJoinWhere", 0);
        put("rightJoin", 0);
        put("rightJoinWhere", 0);
        put("crossJoin", 0);
        put("hasTable", 0);
        put("getColumnListing", 0);
        put("hasColumn", 0);
        put("hasColumns", 0);
        put("getColumnType", 0);
        put("create", 0);
        put("drop", 0);
        put("dropIfExists", 0);
        put("dropColumns", 0);
        put("rename", 0);
        put("assertDatabaseHas", 0);
        put("assertDatabaseMissing", 0);
        put("assertDatabaseCount", 0);
        put("assertDeleted", 0);
        put("assertSoftDeleted", 0);
        put("fromSub", 0);
        put("selectSub", 0);
    }};

    public static Map<String, List<Integer>> BUILDER_METHODS = new HashMap<>() {{
        put("select", List.of(-1));
        put("addSelect", List.of(0));
        put("join", List.of(1, 3));
        put("joinWhere", List.of(1, 3));
        put("joinSub", List.of(2, 4));
        put("leftJoin", List.of(1, 3));
        put("leftJoinWhere", List.of(1, 3));
        put("leftJoinSub", List.of(2, 4));
        put("rightJoin", List.of(1, 3));
        put("rightJoinWhere", List.of(1, 3));
        put("rightJoinSub", List.of(2, 4));
        put("crossJoin", List.of(1, 3));
        put("where", List.of(0));
        put("whereNot", List.of(0));
        put("orWhere", List.of(0));
        put("orWhereNot", List.of(0));
        put("whereColumn", List.of(0, 2));
        put("orWhereColumn", List.of(0, 2));
        put("whereIn", List.of(0));
        put("orWhereIn", List.of(0));
        put("whereNotIn", List.of(0));
        put("orWhereNotIn", List.of(0));
        put("whereIntegerInRaw", List.of(0));
        put("orWhereIntegerInRaw", List.of(0));
        put("orWhereIntegerNotInRaw", List.of(0));
        put("whereNull", List.of(0));
        put("orWhereNull", List.of(0));
        put("whereNotNull", List.of(0));
        put("whereBetween", List.of(0));
        put("whereBetweenColumns", List.of(0));
        put("orWhereBetween", List.of(0));
        put("orWhereBetweenColumns", List.of(0));
        put("whereNotBetween", List.of(0));
        put("whereNotBetweenColumns", List.of(0));
        put("orWhereNotBetween", List.of(0));
        put("orWhereNotBetweenColumns", List.of(0));
        put("orWhereNotNull", List.of(0));
        put("whereDate", List.of(0));
        put("orWhereDate", List.of(0));
        put("whereTime", List.of(0));
        put("orWhereTime", List.of(0));
        put("whereDay", List.of(0));
        put("orWhereDay", List.of(0));
        put("whereMonth", List.of(0));
        put("orWhereMonth", List.of(0));
        put("whereYear", List.of(0));
        put("orWhereYear", List.of(0));
        put("whereRowValues", List.of(0));
        put("orWhereRowValues", List.of(0));
        put("whereJsonContains", List.of(0));
        put("orWhereJsonContains", List.of(0));
        put("whereJsonDoesntContain", List.of(0));
        put("orWhereJsonDoesntContain", List.of(0));
        put("whereJsonLength", List.of(0));
        put("orWhereJsonLength", List.of(0));
        put("groupBy", List.of(-1));
        put("having", List.of(0));
        put("orHaving", List.of(0));
        put("havingBetween", List.of(0));
        put("orderBy", List.of(0));
        put("orderByDesc", List.of(0));
        put("latest", List.of(0));
        put("oldest", List.of(0));
        put("forPageBeforeId", List.of(2));
        put("forPageAfterId", List.of(2));
        put("reorder", List.of(0));
        put("find", List.of(1));
        put("value", List.of(0));
        put("get", List.of(-1));
        put("paginate", List.of(1));
        put("simplePaginate", List.of(1));
        put("getCountForPagination", List.of(0));
        put("pluck", List.of(0, 1));
        put("implode", List.of(0));
        put("count", List.of(0));
        put("min", List.of(0));
        put("max", List.of(0));
        put("sum", List.of(0));
        put("avg", List.of(0));
        put("average", List.of(0));
        put("aggregate", List.of(1));
        put("numericAggregate", List.of(1));
        put("insertUsing", List.of(1));
        put("increment", List.of(0));
        put("decrement", List.of(0));
        put("updateOrInsert", List.of(0, 1));
        put("update", List.of(0));
        put("on", List.of(0, 1, 2));
        put("hasColumn", List.of(1));
        put("hasColumns", List.of(1));
        put("getColumnType", List.of(1));
        put("dropColumn", List.of(0));
        put("dropColumns", List.of(1));
        put("dropConstrainedForeignId", List.of(0));
        put("renameColumn", List.of(0));
        put("dropSoftDeletes", List.of(0));
        put("dropSoftDeletesTz", List.of(0));
        put("unique", List.of(0));
        put("index", List.of(0));
        put("spatialIndex", List.of(0));
        put("foreign", List.of(0));
        put("indexCommand", List.of(1));
        put("createIndexName", List.of(1));
        put("after", List.of(0));
        put("removeColumn", List.of(0));
        put("primary", List.of(0));
        put("dropIndex", List.of(0));
        put("dropUnique", List.of(0));
        put("dropPrimary", List.of(0));
        put("dropForeign", List.of(0));
        put("dropSpatialIndex", List.of(0));
        put("id", List.of(0));
        put("increments", List.of(0));
        put("integerIncrements", List.of(0));
        put("tinyIncrements", List.of(0));
        put("mediumIncrements", List.of(0));
        put("bigIncrements", List.of(0));
        put("char", List.of(0));
        put("string", List.of(0));
        put("text", List.of(0));
        put("mediumText", List.of(0));
        put("longText", List.of(0));
        put("integer", List.of(0));
        put("tinyInteger", List.of(0));
        put("smallInteger", List.of(0));
        put("mediumInteger", List.of(0));
        put("bigInteger", List.of(0));
        put("unsignedInteger", List.of(0));
        put("unsignedTinyInteger", List.of(0));
        put("unsignedSmallInteger", List.of(0));
        put("unsignedMediumInteger", List.of(0));
        put("unsignedBigInteger", List.of(0));
        put("foreignId", List.of(0));
        put("foreignIdFor", List.of(1));
        put("float", List.of(0));
        put("double", List.of(0));
        put("decimal", List.of(0));
        put("unsignedFloat", List.of(0));
        put("unsignedDouble", List.of(0));
        put("unsignedDecimal", List.of(0));
        put("boolean", List.of(0));
        put("enum", List.of(0));
        put("set", List.of(0));
        put("json", List.of(0));
        put("jsonb", List.of(0));
        put("date", List.of(0));
        put("dateTime", List.of(0));
        put("dateTimeTz", List.of(0));
        put("time", List.of(0));
        put("timeTz", List.of(0));
        put("timestamp", List.of(0));
        put("timestampTz", List.of(0));
        put("softDeletes", List.of(0));
        put("softDeletesTz", List.of(0));
        put("year", List.of(0));
        put("binary", List.of(0));
        put("uuid", List.of(0));
        put("foreignUuid", List.of(0));
        put("ipAddress", List.of(0));
        put("macAddress", List.of(0));
        put("geometry", List.of(0));
        put("point", List.of(0));
        put("lineString", List.of(0));
        put("polygon", List.of(0));
        put("geometryCollection", List.of(0));
        put("multiPoint", List.of(0));
        put("multiLineString", List.of(0));
        put("multiPolygon", List.of(0));
        put("multiPolygonZ", List.of(0));
        put("computed", List.of(0));
        put("create", List.of(0));
        put("fill", List.of(0));
        put("updateOrCreate", List.of(0, 1));
        put("assertDatabaseHas", List.of(1));
        put("assertDatabaseMissing", List.of(1));
        put("assertDeleted", List.of(1));
        put("assertSoftDeleted", List.of(1));
        put("orWhereRelation", List.of(1));
        put("whereMorphRelation", List.of(2));
        put("orWhereMorphRelation", List.of(2));
        put("withAggregate", List.of(1));
        put("withMax", List.of(1));
        put("withMin", List.of(1));
        put("withSum", List.of(1));
        put("withAvg", List.of(1));
    }};


    public static final Map<String, Integer> QUERY_RELATION_PARAMS = new HashMap<>() {{
        put("with", 0);
        put("has", 0);
        put("orHas", 0);
        put("doesntHave", 0);
        put("orDoesntHave", 0);
        put("whereHas", 0);
        put("withWhereHas", 0);
        put("orWhereHas", 0);
        put("whereDoesntHave", 0);
        put("orWhereDoesntHave", 0);
        put("hasMorph", 0);
        put("orHasMorph", 0);
        put("doesntHaveMorph", 0);
        put("orDoesntHaveMorph", 0);
        put("whereHasMorph", 0);
        put("orWhereHasMorph", 0);
        put("whereDoesntHaveMorph", 0);
        put("orWhereDoesntHaveMorph", 0);
        put("whereRelation", 0);
        put("orWhereRelation", 0);
        put("whereMorphRelation", 0);
        put("orWhereMorphRelation", 0);
        put("whereMorphedTo", 0);
        put("orWhereMorphedTo", 0);
        put("whereNotMorphedTo", 0);
        put("orWhereNotMorphedTo", 0);
        put("whereBelongsTo", 1);
        put("orWhereBelongsTo", 1);
        put("withAggregate", 0);
        put("withCount", 0);
        put("withMax", 0);
        put("withMin", 0);
        put("withSum", 0);
        put("withAvg", 0);
        put("withExists", 0);
    }};


}

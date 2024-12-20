<?php

/** @noinspection all */

namespace ${namespace} {

   use IdeaWhispererForLaravel\Helper\BaseQueryBuilder;
   use Illuminate\Support\Carbon;
   use Illuminate\Database\Eloquent\Collection as _BaseEloquentCollection;
   use Illuminate\Database\Eloquent\Model;

<#list models as model>
  /**
<#list model.fields as field>
   * @property ${field.type}<#if field.nullable>|null</#if> $${field.name}
</#list>
<#list model.methods as method>
   * @method static ${method.returnType} ${method.name}(<#list method.parameters as parameter>${parameter.toString()}<#if parameter?has_next>, </#if></#list>)
</#list>
<#list model.relations as relation>
   * @method ${relation.returnType} ${relation.name}()
</#list>
   * @method static \Illuminate\Database\Eloquent\Builder onWriteConnection()
   * @method \Illuminate\Database\Eloquent\Builder newQuery()
   * @method static \Illuminate\Database\Eloquent\Builder on(null|string $connection = null)
   * @method static \Illuminate\Database\Eloquent\Builder query()
   * @method static \Illuminate\Database\Eloquent\Builder with(array|string $relations)
   * @method \Illuminate\Database\Eloquent\Builder newModelQuery()
   * @method false|int increment(string $column, float|int $amount = 1, array $extra = [])
   * @method false|int decrement(string $column, float|int $amount = 1, array $extra = [])
   * @method static _BaseEloquentCollection|${model.modelName}[] all()
   * @method static ${model.modelName} create(array $attributes = [])
   * @method static ${model.modelName} createOrFirst(array $attributes = [], array $values = [])
   * @method static _BaseEloquentCollection|${model.modelName}[] cursor()
   * @method static ${model.modelName}[] eagerLoadRelations(array $models)
   * @method static ${model.modelName}|null|${model.modelName}[]|_BaseEloquentCollection find($id, array|string $columns = ['*'])
   * @method static _BaseEloquentCollection|${model.modelName}[] findMany(array|Arrayable $ids, array|string $columns = ['*'])
   * @method static _BaseEloquentCollection|${model.modelName}[] findOr($id, \Closure|string|string[] $columns = ['*'], \Closure|null $callback = null)
   * @method static ${model.modelName}|${model.modelName}[]|_BaseEloquentCollection findOrFail($id, array|string $columns = ['*'])
   * @method static ${model.modelName}|${model.modelName}[]|_BaseEloquentCollection findOrNew($id, array|string $columns = ['*'])
   * @method static ${model.modelName} first(array|string $columns = ['*'])
   * @method static ${model.modelName} firstOr(\Closure|string[] $columns = ['*'], \Closure|null $callback = null)
   * @method static ${model.modelName} firstOrCreate(array $attributes = [], array $values = [])
   * @method static ${model.modelName} firstOrFail(array|string $columns = ['*'])
   * @method static ${model.modelName} firstOrNew(array $attributes = [], array $values = [])
   * @method static ${model.modelName} firstWhere(array|\Closure|Expression|string $column, $operator = null, $value = null, string $boolean = 'and')
   * @method static ${model.modelName} forceCreate(array $attributes)
   * @method static ${model.modelName} forceCreateQuietly(array $attributes = [])
   * @method static _BaseEloquentCollection|${model.modelName}[] fromQuery(string $query, array $bindings = [])
   * @method static _BaseEloquentCollection|${model.modelName}[] get(array|string $columns = ['*'])
   * @method static ${model.modelName} getModel()
   * @method static ${model.modelName}[] getModels(array|string $columns = ['*'])
   * @method static _BaseEloquentCollection|${model.modelName}[] hydrate(array $items)
   * @method static _BaseEloquentCollection|${model.modelName}[] lazy(int $chunkSize = 1000)
   * @method static _BaseEloquentCollection|${model.modelName}[] lazyById(int $chunkSize = 1000, null|string $column = null, null|string $alias = null)
   * @method static _BaseEloquentCollection|${model.modelName}[] lazyByIdDesc(int $chunkSize = 1000, null|string $column = null, null|string $alias = null)
   * @method static ${model.modelName} make(array $attributes = [])
   * @method static ${model.modelName} newModelInstance(array $attributes = [])
   * @method static _BaseEloquentCollection|LengthAwarePaginator|${model.modelName}[] paginate(\Closure|int|null $perPage = null, array|string $columns = ['*'], string $pageName = 'page', int|null $page = null, \Closure|int|null $total = null)
   * @method static _BaseEloquentCollection|Paginator|${model.modelName}[] simplePaginate(int|null $perPage = null, array|string $columns = ['*'], string $pageName = 'page', int|null $page = null)
   * @method static ${model.modelName} sole(array|string $columns = ['*'])
   * @method static ${model.modelName} updateOrCreate(array $attributes, array $values = [])
   * @mixin BaseQueryBuilder
   */
   class ${model.modelName} extends Model {}

</#list>
}
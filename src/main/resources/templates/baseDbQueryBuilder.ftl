<?php

/** @noinspection all */

namespace IdeaWhispererForLaravel\Helper {

    use Illuminate\Contracts\Database\Query\Expression;
    use Illuminate\Contracts\Support\Arrayable;
    use Illuminate\Database\ConnectionInterface;
    use Illuminate\Database\Query\Builder;
    use Illuminate\Database\Eloquent\Builder as EloquentBuilder;

   /**
<#list methods as method>
   * @see ${method.see}::${method.name}
   * @method ${method.returnType} ${method.name}(<#list method.parameters as parameter>${parameter.text}<#if parameter?has_next>, </#if></#list>)
</#list>
   */
   class BaseQueryBuilder extends EloquentBuilder {}
}
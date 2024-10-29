<?php

/** @noinspection all */

namespace Illuminate\Support\Facades {

<#list facades as facade>
   /**
<#list facade.methods as method>
   * @see ${method.see}::${method.name}
   * @method static ${method.returnType} ${method.name}(<#list method.parameters as parameter>${parameter.text}<#if parameter?has_next>, </#if></#list>)
</#list>
   */
   class ${facade.facadeName} {}

</#list>
}

namespace {
<#list facades as facade>
   class ${facade.facadeName} extends ${facade.namespace} {}
</#list>
}
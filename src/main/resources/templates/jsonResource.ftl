<?php

namespace ${namespace};

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;
<#if model??>use ${model.namespace}\${model.name};</#if>

<#if model??>/** @mixin ${model.name} */</#if>
class ${name} extends JsonResource
{
    public function toArray(Request $request): array
    {
    <#if model??>
        return [
        <#if model.fields??>
        <#list model.fields as field>
            '${field.name}' => $this->${field.name},
        </#list>
        ];
        </#if>
    <#else>
        return [];
    </#if>
    }
}
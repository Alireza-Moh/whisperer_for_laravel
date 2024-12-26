<?php

namespace ${namespace};

use Illuminate\Database\Eloquent\Factories\Factory;
<#if model??>use ${modelNamespace};</#if>

class ${name} extends Factory
{
    <#if model??>protected $model = ${model.name}::class;</#if>

    public function definition(): array
    {
    <#if model??>
        return [
        <#if model.fields??>
        <#list model.fields as field>
            '${field.name}' => $this->faker->name(),
        </#list>
        ];
        </#if>
    <#else>
        return [];
    </#if>
    }
}
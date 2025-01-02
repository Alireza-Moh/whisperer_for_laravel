<?php

namespace ${namespace};

use Illuminate\Foundation\Http\FormRequest;

class ${name} extends FormRequest
{
<#if authorize??>
    public function authorize(): bool
    {
        return true;
    }
</#if>

    public function rules(): array
    {
    <#if model??>
        return [
        <#if model.fields??>
        <#list model.fields as field>
            '${field.name}' => 'required',
        </#list>
        ];
    </#if>
    <#else>
        return [];
    </#if>
    }
}
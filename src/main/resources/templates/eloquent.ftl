<?php

namespace ${namespace};

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
<#if dbFactoryModel??>use ${dbFactoryModel.namespace}\${dbFactoryModel.name};</#if>

class ${name} extends Model {

<#if dbFactoryModel??>
    /** @use HasFactory<${dbFactoryModel.name}> */
    use HasFactory;
</#if>

    /**
     * The attributes that are mass assignable.
     * @var list<string>
     */
    protected $fillable = [
    <#list fields as field>
        '${field.getName()}',
    </#list>
    ];
}
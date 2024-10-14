<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

<#if anonymous>
return new class extends Migration
<#else>
class ${name} extends Migration
</#if>
{
    /**
     * Run the migrations
     */
    public function up(): void
    {
<#if createTable>
        Schema::create('${tableName}', function (Blueprint $table) {
        <#if hasFields>
        <#list fields as field>
            $table->${field.getType()}('${field.getName()}')<#if field.isNullable()>->nullable(${field.getNullableString()})</#if>;
        </#list>
            $table->timestamps();
        </#if>
        });
<#else>
        Schema::table('${tableName}', function (Blueprint $table) {
            //
        });
</#if>
    }

    public function down(): void
    {
        Schema::dropIfExists('${tableName}');
    }
};
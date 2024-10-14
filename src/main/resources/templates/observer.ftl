<?php

namespace ${namespace};

<#if hasModel>use ${eloquentModelPath};</#if>

<#if !hasModel>
class ${name}
{

}
<#else>
class ${name} {
<#if addCreatingMethod>
    public function creating(${eloquentModelName} $${eloquentModelNameVariable}): void {}
</#if>

<#if addCreatedMethod>
    public function created(${eloquentModelName} $${eloquentModelNameVariable}): void {}
</#if>

<#if addUpdatingMethod>
    public function updating(${eloquentModelName} $${eloquentModelNameVariable}): void {}
</#if>

<#if addUpdatedMethod>
    public function updated(${eloquentModelName} $${eloquentModelNameVariable}): void {}
</#if>

<#if addSavingMethod>
    public function saving(${eloquentModelName} $${eloquentModelNameVariable}): void {}
</#if>

<#if addSavedMethod>
    public function saved(${eloquentModelName} $${eloquentModelNameVariable}): void {}
</#if>

<#if addDeletingMethod>
    public function deleting(${eloquentModelName} $${eloquentModelNameVariable}): void {}
</#if>

<#if addDeletedMethod>
    public function deleted(${eloquentModelName} $${eloquentModelNameVariable}): void {}
</#if>

<#if addRestoringMethod>
    public function restoring(${eloquentModelName} $${eloquentModelNameVariable}): void {}
</#if>

<#if addRestoredMethod>
    public function restored(${eloquentModelName} $${eloquentModelNameVariable}): void {}
</#if>

<#if addRetrievedMethod>
    public function retrieved(${eloquentModelName} $${eloquentModelNameVariable}): void {}
</#if>

<#if addForceDeletingMethod>
    public function forceDeleting(${eloquentModelName} $${eloquentModelNameVariable}): void {}
</#if>

<#if addForceDeletedMethod>
    public function forceDeleted(${eloquentModelName} $${eloquentModelNameVariable}): void {}
</#if>

<#if addReplicatingMethod>
    public function replicating(${eloquentModelName} $${eloquentModelNameVariable}): void {}
</#if>
}
</#if>
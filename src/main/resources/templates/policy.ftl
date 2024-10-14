<?php

namespace ${namespace};

use Illuminate\Auth\Access\HandlesAuthorization;

use App\Models\User;
<#if hasModel>use ${eloquentModelPath};</#if>

class ${name}
{
    use HandlesAuthorization;

<#if hasModel>
    public function viewAny(User $user, ${eloquentModelName} $${eloquentModelNameVariable}): bool
    {

    }

    public function view(User $user, ${eloquentModelName} $${eloquentModelNameVariable}): bool
    {
    }

    public function create(User $user): bool
    {
    }

    public function update(User $user, ${eloquentModelName} $${eloquentModelNameVariable}): bool
    {
    }

    public function delete(User $user, ${eloquentModelName} $${eloquentModelNameVariable}): bool
    {
    }

    public function restore(User $user, ${eloquentModelName} $${eloquentModelNameVariable}): bool
    {
    }

    public function forceDelete(User $user, ${eloquentModelName} $${eloquentModelNameVariable}): bool
    {
    }

<#else>
    public function action(User $user): bool
    {
    }
</#if>
}

<?php

namespace ${namespace};

use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Scope;

class ${name} implements Scope
{
    public function apply(Builder $builder, Model $model)
    {
        $builder->where();
    }
}
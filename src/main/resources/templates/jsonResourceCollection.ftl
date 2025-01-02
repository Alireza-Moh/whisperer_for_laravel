<?php

namespace ${namespace};

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\ResourceCollection;

class ${name} extends ResourceCollection
{
    public function toArray(Request $request): array
    {
        return [
            'data' => $this->collection,
        ];
    }
}
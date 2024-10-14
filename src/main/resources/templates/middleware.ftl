<?php

namespace ${namespace};

use Closure;
use Illuminate\Http\Request;

class ${name}
{
    public function handle(Request $request, Closure $next)
    {
        return $next($request);
    }
}
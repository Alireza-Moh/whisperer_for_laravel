<?php

namespace ${namespace};

use Closure;
use Illuminate\View\Component;
use Illuminate\Contracts\View\View;

class ${name} extends Component
{
    /**
     * Create a new component instance.
     */
    public function __construct()
    {
        //
    }

    /**
     * Get the viewModel
     */
    public function render(): View|Closure|string
    {
        return view('');
    }
}
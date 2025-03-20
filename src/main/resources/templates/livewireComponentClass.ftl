<?php

namespace ${namespace};

use Livewire\Component;

class ${name} extends Component
{
    public function render()
    {
    <#if inline>
        return <<<'HTML'
            <div>
                <!--  code  -->
            </div>
        HTML;
    <#else>
        return view('${viewName}');
    </#if>

    }
}


<?php

namespace ${namespace};

<#if hasEventName>
use ${eventClassPath};
</#if>

class ${name}
{
    public function __construct() {}

    public function handle(<#if hasEventName>${eventClassName} $event</#if>): void {}
}

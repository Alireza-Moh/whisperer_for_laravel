<#if vue>
<#if withOptionsApi>
<script>
    export default {
        name: '${pageName}'
    }
</script>

<template>

</template>

<style scoped>

</style>
<#else>
<script setup>

</script>

<template>

</template>

<style scoped>

</style>
</#if>
<#else>
import React from 'react';

const ${pageName} = () => {
    return (
        <div>
            {/* Your component markup goes here */}
        </div>
    );
};

export default ${pageName};
</#if>
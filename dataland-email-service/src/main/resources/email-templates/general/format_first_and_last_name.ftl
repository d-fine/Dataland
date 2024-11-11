<#macro formatFirstAndLastName firstName lastName>
<#if firstName?? || lastName??>The user <#if firstName??>${firstName} </#if><#if lastName??>${lastName} </#if><#else>A user </#if>
</#macro>
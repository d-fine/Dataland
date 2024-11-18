<#macro formatFirstAndLastName firstName="" lastName="">
    <#if firstName?has_content && lastName?has_content>
        The user ${firstName} ${lastName}<#t>
    <#elseif firstName?has_content>
        The user ${firstName}<#t>
    <#elseif lastName?has_content>
        The user ${lastName}<#t>
    <#else>
        A user<#t>
    </#if>
</#macro>
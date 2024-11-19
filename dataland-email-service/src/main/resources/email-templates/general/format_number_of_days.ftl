<#macro formatNumberOfDays numberOfDays>
    <#if numberOfDays?has_content>
        <#if numberOfDays == 0>
            today<#t>
        <#elseif numberOfDays == 1>
            in the last day<#t>
        <#else>
            in the last ${numberOfDays} days<#t>
        </#if>
    <#else>
        in the last days<#t>
    </#if>
</#macro>
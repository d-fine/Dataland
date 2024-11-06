<#macro formatNumberOfDays numberOfDays>
    <#if numberOfDays?has_content>
        <#if numberOfDays == 0>
            today
        <#elseif numberOfDays == 1>
            in the last day
        <#else>
            in the last ${numberOfDays} days
        </#if>
    <#else>
        in the last days
    </#if>
</#macro>
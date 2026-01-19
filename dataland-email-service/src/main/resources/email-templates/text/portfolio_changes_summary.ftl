<#macro renderTable(data)>
<#assign previousFramework = "">
<#list data as item>
    <#if (item_index != 0) && (item.dataTypeLabel != previousFramework)>

    ------------------------

    <#else></#if>
    <#if item.dataTypeLabel != previousFramework>Framework: ${item.dataTypeLabel}</#if>

        Reporting period: ${item.reportingPeriod}
        <#if (item.companies?size > 1)>Companies<#else>Company</#if>:
        <#list item.companies as company>
            <#if company_index < 5>
                ${company}
                <#if company_index == 4 && (item.companies?size > 5)>
                    and more
                </#if>
            </#if>
        </#list>
    <#assign previousFramework = item.dataTypeLabel>
</#list>
</#macro>
${frequency} summary for your portfolio(s): ${portfolioNamesString}

Data for your portfolio(s) has been updated on Dataland.
You'll find a summary of these updates in the overview below.

Check your portfolios using this link:
VIEW MY PORTFOLIOS: [${baseUrl}/portfolios]

<#if newData?? && newData?has_content>
----------------------------
New Data
----------------------------

<@renderTable data=newData />


</#if>
<#if updatedData?? && updatedData?has_content>
----------------------------
Updated Data
----------------------------

<@renderTable data=updatedData />


</#if>
<#if nonSourceableData?? && nonSourceableData?has_content>
----------------------------
Non-sourceable Data
----------------------------

<@renderTable data=nonSourceableData />
</#if>
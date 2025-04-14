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
Weekly Summary 📣

Data for your request(s) has been updated on Dataland this week, listed in the sections below.
Please note that you may have already reviewed these updates.

Check details for all your requests using the link below.
VIEW MY DATA REQUESTS: [${baseUrl}/requests]

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
<#macro renderTable(data)>
<#assign previousFramework = "">
<#list data as item>
    <#if (item_index != 0) && (item.framework != previousFramework)>

    ------------------------

    <#else></#if>
    <#if item.framework != previousFramework>Framework: ${item.framework}</#if>

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
    <#assign previousFramework = item.framework>
</#list>
</#macro>


Weekly Summary 📣

Data for your request(s) has been updated on Dataland this week.
Please note that you may have already reviewed these updates.

----------------------------
New Data
----------------------------

<@renderTable data=newData />

VIEW NEW DATA ON MY DATA REQUESTS:
[${baseUrl}/requests]


----------------------------
Updated Data
----------------------------

<@renderTable data=updatedData />

VIEW UPDATED DATA ON MY DATA REQUESTS:
[${baseUrl}/requests]


----------------------------
Non-sourceable Data
----------------------------

<@renderTable data=nonSourceableData />

VIEW NON SOURCEABLE DATA ON MY DATA REQUESTS:
[${baseUrl}/requests]
<#include "general_makros_html.ftl">
<#macro renderTable(data)>
<div style="background-color: #f6f6f6; padding: 20px;">
    <table style="background-color: #f6f6f6; border-collapse: collapse; margin: 0; width: 100%">
        <tbody>
        <#assign previousFramework = "">
        <#list data as item>
            <#if (item_index != 0) && (item.framework != previousFramework)>
                    <@spacerRowHorizontalLineTop />
            </#if>
            <tr>
                <td>
                    <#if item.framework != previousFramework>Framework</#if>
                </td>
                <td>Reporting period</td>
                <td>
                    <#if (item.companies?size > 1)>Companies<#else>Company</#if>
                </td>
            </tr>
            <@spacerRowTiny />
            <tr>
                <td style="vertical-align: top; font-weight: bold; font-size:19px">
                    <#if item.framework != previousFramework>${item.framework}</#if>
                </td>
                <td style="vertical-align: top; font-weight: bold; font-size:19px">${item.reportingPeriod}</td>
                <td style="vertical-align: top; font-weight: bold; font-size:19px">
                    <#list item.companies as company>
                        <#if company_index < 5><div>${company}</div></#if>
                    </#list>
                    <#if (item.companies?size > 5)><div>and more</div></#if>
                </td>
            </tr>
            <#if item_index + 1 != data?size>
                <@spacerRow />
            </#if>
            <#assign previousFramework = item.framework>
        </#list>
        </tbody>
    </table>
</div>
</#macro>

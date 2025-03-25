<#include "../general/general_makros_html.ftl">

<#---------------------------------->
<!-- MACRO USED ONLY IN THIS FILE -->
<#---------------------------------->
<#macro renderDataRequestSummaryTable(data)>
    <div style="background-color: #f6f6f6; padding: 20px; border-radius: 15px">
        <table style="background-color: #f6f6f6; padding: 20px; border-collapse: collapse; margin: 0; width: 100%">
            <tbody>
            <#assign previousFramework = "">
            <#list data as item>
                <#if (item_index != 0) && (item.dataTypeLabel != previousFramework)>
                    <@spacerRowHorizontalLine position="top"/>
                </#if>
                <tr style="height: 30px; padding-bottom: 12px;">
                    <td>
                        <#if item.dataTypeLabel != previousFramework>Framework</#if>
                    </td>
                    <td>Reporting period</td>
                    <td>
                        <#if (item.companies?size > 1)>Companies<#else>Company</#if>
                    </td>
                </tr>
                <tr style="padding-bottom: 12px;">
                    <td style="vertical-align: top; font-weight: bold; font-size:19px">
                        <#if item.dataTypeLabel != previousFramework>${item.dataTypeLabel}</#if>
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
                <#assign previousFramework = item.dataTypeLabel>
            </#list>
            </tbody>
        </table>
    </div>
</#macro>

<#---------------------->
<!-- TEMPLATE CONTENT -->
<#---------------------->
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="color-scheme" content="light">
    <meta name="supported-color-schemes" content="light">
    <title>DATALAND</title>
</head>
<body style="background-color:#DADADA; height: 100%; margin: 0; padding: 0; width: 100%;">
    <#include "../general/header.ftl">
    <table style="background-color: #ffffff; width: 600px; font-family: Arial, Helvetica, sans-serif; font-size: 14px; text-align: left; border-collapse: collapse; padding: 0 20px; margin: 0">
        <tbody>
        <tr>
            <td rowspan="20" style="width: 40px">&nbsp;</td>
            <td colspan="3" style="width: 520px">&nbsp;</td>
            <td rowspan="20" style="width: 40px">&nbsp;</td>
        </tr>
        <!-- EMAIL TEXT -->
        <tr>
            <td colspan="3">Weekly Summary 📣
                <br><br>Data for your request(s) has been updated on Dataland this week,
                listed in the sections below. Please note that you may have already reviewed these updates.
                <br><br>Check details for all your requests using the following link:</td>
        </tr>
        <@spacerRow/>
        <@buttonLink url="${baseUrl}/requests" linkText="VIEW MY DATA REQUESTS" />
        <@spacerRow/>
        <!-- NEW DATA -->
        <#if newData?exists && (newData?size > 0)>
            <@boldTitle title="New Data" />
            <@spacerRow/>
            <tr> <td colspan="3"> <@renderDataRequestSummaryTable data=newData /> </td> </tr>
            <@spacerRow/>
            <@spacerRow/>
        </#if>
        <!-- UPDATED DATA -->
        <#if updatedData?exists && (updatedData?size > 0)>
            <@boldTitle title="Updated Data" />
            <@spacerRow/>
            <tr> <td colspan="3"> <@renderDataRequestSummaryTable data=updatedData /> </td> </tr>
            <@spacerRow/>
            <@spacerRow/>
        </#if>
        <!-- NON SOURCEABLE DATA -->
        <#if nonSourceableData?exists && (nonSourceableData?size > 0)>
            <@boldTitle title="Non sourceable Data" />
            <@spacerRow/>
            <tr> <td colspan="3"> <@renderDataRequestSummaryTable data=nonSourceableData /> </td> </tr>
            <@spacerRow/>
            <@spacerRow/>
        </#if>
        </tbody>
    </table>
    <#include "../general/footer.ftl">
</body>
</html>

<#include "../general/general_macros_html.ftl">

<#---------------------------------->
<!-- MACRO USED ONLY IN THIS FILE -->
<#---------------------------------->
<#macro renderDataRequestSummaryTable(data)>
    <table style="background-color: #f6f6f6; border-radius: 15px; border-collapse: collapse; padding: 0; margin: 0; width: 520px">
        <tbody>
        <tr>
            <td style="padding: 10px;"> </td>
            <td>
                <table style="width: 100%;">
                    <tbody>
                        <#assign previousFramework = "">
                        <@spacerRow/>
                        <#list data as item>
                            <#if (item_index != 0) && (item.dataTypeLabel != previousFramework)>
                                <@spacerRowHorizontalLine position="top" width="480px"/>
                            </#if>
                            <tr style="height: 20px;">
                                <@dataLabelCell label="${(item.dataTypeLabel != previousFramework)?string('Framework', '')}" padding="0"/>
                                <@dataLabelCell label="Reporting Period"/>
                                <@dataLabelCell label="${(item.companies?size > 1)?string('Companies', 'Company')}" padding="0"/>
                            </tr>
                            <@spacerRowTiny/>
                            <tr>
                                <@dataValueCell value="${(item.dataTypeLabel != previousFramework)?string('${item.dataTypeLabel}', '')}"/>
                                <@dataValueCell value="${item.reportingPeriod}"/>
                                <td style="vertical-align: top; font-weight: bold; font-size:19px; padding: 0">
                                    <#list item.companies as company>
                                        <#if company_index < 5><div>${company}</div></#if>
                                    </#list>
                                    <#if (item.companies?size > 5)><div>and more</div></#if>
                                </td>
                            </tr>
                            <@spacerRow />
                            <#assign previousFramework = item.dataTypeLabel>
                        </#list>
                    </tbody>
                </table>
            </td>
            <td style="padding: 10px;"> </td>
        </tbody>
    </table>
</#macro>

<#---------------------->
<!-- TEMPLATE CONTENT -->
<#---------------------->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="color-scheme" content="light">
    <meta name="supported-color-schemes" content="light">
    <title>DATALAND</title>
</head>
<body style="background-color:#DADADA; height: 100%; margin: 0; padding: 0; width: 100%;">
    <#include "../general/header.ftl">
    <table style="background-color: #ffffff; width: 600px; font-family: Arial, Helvetica, sans-serif; font-size: 16px; line-height: 24px; text-align: left; border-collapse: collapse; padding: 0 10px; margin: 0">
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
        <#if newData?? && (newData?size > 0)>
            <@boldTitle title="New Data" />
            <@spacerRow/>
            <tr> <td colspan="3"> <@renderDataRequestSummaryTable data=newData /> </td> </tr>
            <@spacerRow/>
            <@spacerRow/>
        </#if>
        <!-- UPDATED DATA -->
        <#if updatedData?? && (updatedData?size > 0)>
            <@boldTitle title="Updated Data" />
            <@spacerRow/>
            <tr> <td colspan="3"> <@renderDataRequestSummaryTable data=updatedData /> </td> </tr>
            <@spacerRow/>
            <@spacerRow/>
        </#if>
        <!-- NON SOURCEABLE DATA -->
        <#if nonSourceableData?? && (nonSourceableData?size > 0)>
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

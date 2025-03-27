<#include "../general/general_makros_html.ftl">

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
<table style="background-color: #ffffff; width: 600px; font-family: Arial, Helvetica, sans-serif; font-size: 16px; line-height: 24px; text-align: left; border-collapse: collapse; padding: 0 10px; margin: 0">
    <tbody>
    <tr>
        <td rowspan="15" style="width: 40px">&nbsp;</td>
        <td colspan="3" style="width: 520px">&nbsp;</td>
        <td rowspan="15" style="width: 40px">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3">
            We are Dataland, an open, neutral, and transparent data engagement platform.
            One of our members has specifically requested data about your company.
            A data provider within our network has created
            ${(frameworkData?size == 1)?string('a dataset', 'multiple datasets')}
            for your company, which is now accessible on Dataland:
        </td>
    </tr>
    <@spacerRow/>
    <tr>
        <td colspan="3">
            <table style="background-color: #f6f6f6; border-collapse: collapse; border-radius: 15px; padding:0; margin: 0; width: 520px">
                <tbody>
                    <tr>
                        <td style="padding: 10px; width: 20px"> </td>
                        <td>
                            <table style="width: 100%;">
                                <tbody>
                                    <@spacerRow/>
                                    <#list frameworkData as framework>
                                        <@dataLabel label="Framework"/>
                                        <@spacerRowTiny/>
                                        <@dataValue value=framework.dataTypeLabel/>
                                        <@spacerRow/>
                                        <@dataLabel label="Reporting year${(framework.reportingPeriods?size > 1)?string('s', '')}"/>
                                        <@spacerRowTiny/>
                                        <@dataValue value=framework.reportingPeriods?join(", ")/>
                                        <#if !framework?is_last>
                                            <@spacerRowHorizontalLine padding="0 20px" width="480px"/>
                                        </#if>
                                        <@spacerRow/>
                                    </#list>
                                </tbody>
                            </table>
                        </td>
                        <td style="padding: 10px; width: 20px"> </td>
                    </tr>
                </tbody>
            </table>
        </td>
    </tr>
    <@spacerRow/>
    <@spacerRowTiny/>
    <@howToProceed items=["Gain sovereignty over your data by claiming company ownership.",
                          "Inspect, add, correct, remove data of your company."]/>
    <@spacerRow/>
    <@buttonLink url="${baseUrl}/companies/${companyId}" linkText="CLAIM COMPANY OWNERSHIP" />
    <@spacerRow/>
    <tr>
        <td colspan="3">Claiming ownership process usually requires 1-2 business days.<br />
            You will be notified by email.</td>
    </tr>
    <@spacerRow/>
    </tbody>
</table>
<#include "../general/your_benefits_with_dataland.ftl">
<#include "../general/footer.ftl">
<#include "../general/unsubscribe.ftl">
</body>
</html>
<#macro if if then else><#if if>${then}<#else>${else}</#if></#macro>
<#include "../general/format_first_and_last_name.ftl">
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

<table style="background-color: #ffffff; width: 600px; font-family: Arial, Helvetica, sans-serif; font-size: 14px; text-align: left; border-collapse: collapse; padding: 0; margin: 0">
    <tbody>
    <tr>
        <td rowspan="15" style="width: 40px">&nbsp;</td>
        <td colspan="3" style="width: 520px">&nbsp;</td>
        <td rowspan="15" style="width: 40px">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3">Exciting news! 📣<br>Your data are in high demand on Dataland!
            <@formatFirstAndLastName requesterFirstName requesterLastName/> is requesting access to your data from ${companyName} on dataland.</td>
    </tr>
    <tr>
        <td colspan="3" style="height: 20px">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3">
            <table style="background-color: #f6f6f6; border-collapse: collapse; padding: 0; margin: 0; width: 520px">
                <tbody>
                <tr>
                    <td style="text-align: left; width: 20px; height: 13px; padding: 0">
                    </td>
                    <td style="text-align: right; width: 480px; padding: 0;">
                    </td>
                    <td style="text-align: right; width: 20px; padding: 0">
                    </td>
                </tr>
                <tr>
                    <td rowspan="<@if message?? 18 14 />">&nbsp;</td>
                    <td style="height: 7px"></td>
                    <td rowspan="<@if message?? 18 14 />">&nbsp;</td>
                </tr>
                <tr>
                    <td>Framework</td>
                </tr>
                <tr>
                    <td style="font-size: 5px; height: 5px">&nbsp;</td>
                </tr>
                <tr>
                    <td style="font-weight: bold; font-size:19px">${dataTypeLabel}</td>
                </tr>
                <tr>
                    <td style="height: 20px">&nbsp;</td>
                </tr>
                <tr>
                    <td>Reporting year<#if (reportingPeriods?size > 1)>s</#if></td>
                </tr>
                <tr>
                    <td style="font-size: 5px; height: 5px">&nbsp;</td>
                </tr>
                <tr>
                    <td style="font-weight: bold; font-size:19px">${reportingPeriods?join(", ")}</td>
                </tr>
                <tr>
                    <td style="border-bottom:1px solid #e3e3e3; height: 20px">&nbsp;</td>
                </tr>
                <tr>
                    <td style="height: 20px">&nbsp;</td>
                </tr>
                <tr>
                    <td style="color:#8c8c8c">From:</td>
                </tr>
                <tr>
                    <td style="font-size: 5px; height: 5px">&nbsp;</td>
                </tr>
                <tr>
                    <td style="font-weight: bold; color: #FF6813; text-decoration: none; border: 0 none;">
                        <#if requesterEmail??>
                            <a href="mailto:${requesterEmail}" style="font-weight: bold; color: black; text-decoration: none; border: 0 none;">
                                ${requesterEmail}
                            </a>
                        </#if>
                    </td>
                </tr>
                <#if message??>
                    <tr>
                        <td style="height: 20px">&nbsp;</td>
                    </tr>
                    <tr>
                        <td style="color:#8c8c8c">Message:</td>
                    </tr>
                    <tr>
                        <td style="font-size: 5px; height: 5px">&nbsp;</td>
                    </tr>
                    <tr>
                        <td style="font-weight: bold;">${message}</td>
                    </tr>
                </#if>
                <tr>
                    <td style="height: 7px"></td>
                </tr>

                <tr>
                    <td style="text-align: left; width: 20px; height: 13px; padding: 0"></td>
                    <td style="text-align: right; width: 480px; padding: 0"></td>
                    <td style="text-align: right; width: 20px; padding: 0"></td>
                </tr>
                </tbody>
            </table>
        </td>
    </tr>
    <tr>
        <td colspan="3" style="height: 40px">&nbsp;</td>
    </tr>

    <tr>
        <td colspan="3" style="font-weight: bold;">How to proceed?</td>
    </tr>
    <tr>
        <td colspan="3" style="font-size: 5px; height: 5px">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3">
            <ol style="line-height: 25px;">
                <li>Verify the access request on dataland.</li>
                <li>If you want to share the dataset, grant access.</li>
            </ol>
        </td>
    </tr>
    <tr>
        <td colspan="3" style="height: 20px">&nbsp;</td>
    </tr>
    <tr>
        <td style="text-align: left; padding:0; margin:0; border: 0; height: 54px; width: 26px"></td>
        <td style="background-color: #ff5c00; text-align: center; padding:0; margin:0; border: 0; height: 54px; width: 468px;">
            <a href="${baseUrl}/companyrequests" target="_blank" style="border: 0 none; line-height: 30px; color: #ffffff; font-size: 18px; width: 100%; display: block; text-decoration: none;">
                VERIFY AND GRANT ACCESS ON DATALAND
            </a>
        </td>
        <td style="background-color: #ffffff; text-align: right; padding:0; margin:0; border: 0; height: 54px; width: 26px"></td>
    </tr>

    <tr>
        <td colspan="3" style="height: 20px">&nbsp;</td>
    </tr>
    </tbody>
</table>

<#include "../general/why_me.ftl">
<#include "../general/footer.ftl">

</body>
</html>

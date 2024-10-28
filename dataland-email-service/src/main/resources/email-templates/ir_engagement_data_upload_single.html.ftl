<#macro if if then else><#if if>${then}<#else>${else}</#if></#macro>


<#-- This is a template for the IR engament email triggered by a single data upload
 The following fields are needed
  - companyName
  - companyId
  - framework (the framework name)
  - year (the year of the upload)
  - baseUrl (something like https://dataland.com)
 -->

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="color-scheme" content="light">
    <meta name="supported-color-schemes" content="light">
    <title>DATALAND</title>
</head>
<body style="background-color:#DADADA; height: 100%; margin: 0; padding: 0; width: 100%;">
<#include "./general/header.ftl">

<table style="background-color: #ffffff; width: 600px; font-family: Arial, Helvetica, sans-serif; font-size: 14px; text-align: left; border-collapse: collapse; padding: 0; margin: 0">
    <tbody>
    <tr>
        <td rowspan="15" style="width: 40px">&nbsp;</td>
        <td colspan="3" style="width: 520px">&nbsp;</td>
        <td rowspan="15" style="width: 40px">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3">Exciting news! 📣<br>Data for ${companyName} has been uploaded to Dataland!</td>
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
                    <td rowspan="11">&nbsp;</td>
                    <td style="height: 7px"></td>
                    <td rowspan="11">&nbsp;</td>
                </tr>
                <tr>
                    <td>Framework</td>
                </tr>
                <tr>
                    <td style="font-size: 5px; height: 5px">&nbsp;</td>
                </tr>
                <tr>
                    <td style="font-weight: bold; font-size:19px">${framework}</td>
                </tr>
                <tr>
                    <td style="height: 20px">&nbsp;</td>
                </tr>
                <tr>
                    <td>Reporting year</td>
                </tr>
                <tr>
                    <td style="font-size: 5px; height: 5px">&nbsp;</td>
                </tr>
                <tr>
                    <td style="font-weight: bold; font-size:19px">${year}</td>
                </tr>
                <tr>
                    <td style="height: 20px"></td>
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
                <li>Gain sovereignty over your data by claiming company ownership.</li>
                <li>Inspect, add, correct, remove data of your company.</li>
            </ol>
        </td>
    </tr>
    <tr>
        <td colspan="3" style="height: 20px">&nbsp;</td>
    </tr>
    <tr>
        <td style="text-align: left; padding:0; margin:0; border: 0; height: 54px; width: 26px"></td>
        <td style="background-color: #ff5c00; text-align: center; padding:0; margin:0; border: 0; height: 54px; width: 468px;">
            <a href="${baseUrl}/companies/${companyId}" target="_blank" style="border: 0 none; line-height: 30px; color: #ffffff; font-size: 18px; width: 100%; display: block; text-decoration: none;">
                CLAIM COMPANY OWNERSHIP
            </a>
        </td>
        <td style="background-color: #ffffff; text-align: right; padding:0; margin:0; border: 0; height: 54px; width: 26px"></td>
    </tr>

    <tr>
        <td colspan="3" style="height: 20px">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3">Claiming ownership process usually requires 1-2 business days.<br />
            You will be notified by email</td>
    </tr>
    <tr>
        <td colspan="3" style="height: 20px">&nbsp;</td>
    </tr>
    </tbody>
</table>

<#include "./general/why_me.ftl">
<#include "./general/footer.ftl">
<#include "./general/unsubscribe.ftl">

</body>
</html>

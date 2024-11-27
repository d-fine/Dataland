<#macro if if then else><#if if>${then}<#else>${else}</#if></#macro>

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
        <td rowspan="25" style="width: 40px">&nbsp;</td>
        <td colspan="3" style="width: 520px">&nbsp;</td>
        <td rowspan="25" style="width: 40px">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3">
            Unfortunately, your requested dataset has been labeled as non-sourceable by the data provider.<br>
            We will continue to check the status of your request regularly and inform you in case the dataset will be uploaded in the future.<br>
            View details on the
            <a href="${baseUrl}/requests/${dataRequestId}" target="_blank">
                request page.
            </a>
        </td>
    </tr>
    <tr>
        <td colspan="3" style="height: 20px">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3">
            <#include "../general/display_request.ftl">
        </td>
    <tr>
        <td style="height: 20px">&nbsp;</td>
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
                <li>Review the provided data.</li>
                <li>Do one of the following.</li>
                <li>
                    <ul>
                        <li>Do nothing.</li>
                        <li>Withdraw your request.</li>
                        <li>Reopen the request (in case you are certain this dataset should exist).</li>
                    </ul>
                </li>
            </ol>
        </td>
    </tr>
    <tr>
        <td colspan="3" style="height: 20px">&nbsp;</td>
    </tr>
    <tr>
        <td style="text-align: left; padding:0; margin:0; border: 0; height: 54px; width: 26px"></td>
        <td style="background-color: #ff5c00; text-align: center; padding:0; margin:0; border: 0; height: 54px; width: 468px;">
            <a href="${baseUrl}/requests/${dataRequestId}" target="_blank" style="border: 0 none; line-height: 30px; color: #ffffff; font-size: 18px; width: 100%; display: block; text-decoration: none;">
                REVIEW YOUR REQUEST
            </a>
        </td>
        <td style="background-color: #ffffff; text-align: right; padding:0; margin:0; border: 0; height: 54px; width: 26px"></td>
    </tr>
    <tr>
        <td colspan="3" style="height: 20px">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3" style="font-size: 5px; height: 5px">&nbsp;</td>
    </tr>

    <tr>
        <td colspan="3" style="height: 20px">&nbsp;</td>
    </tr>
    </tbody>
</table>
<#include "../general/footer.ftl">

</body>
</html>

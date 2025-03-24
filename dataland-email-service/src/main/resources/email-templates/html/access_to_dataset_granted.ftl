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
<table style="background-color: #ffffff; width: 600px; font-family: Arial, Helvetica, sans-serif; font-size: 14px; text-align: left; border-collapse: collapse; padding: 0; margin: 0">
    <tbody>
    <tr>
        <td rowspan="15" style="width: 40px">&nbsp;</td>
        <td colspan="3" style="width: 520px">&nbsp;</td>
        <td rowspan="15" style="width: 40px">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3">Great news! 📣 <br>You have now access to the following dataset on Dataland.</td>
    </tr>
    <@spacerRow/>
    <tr>
        <td colspan="3">
            <#include "../general/data_request_information_html.ftl">
        </td>
    </tr>
    <@spacerRow/>
    <@spacerRowTiny/>
    <@howToProceedDataRequest/>
    <@spacerRowTiny/>
    <@buttonLink url="${baseUrl}/companies/${companyId}/frameworks/${dataType}" linkText="REVIEW THE PROVIDED DATA" />
    <@spacerRow/>
    <@spacerRow/>
    </tbody>
</table>
<#include "../general/footer.ftl">
</body>
</html>

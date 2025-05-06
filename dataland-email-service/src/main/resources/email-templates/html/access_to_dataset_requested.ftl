<#include "../general/general_macros_html.ftl">
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
<table style="background-color: #ffffff; width: 600px; font-family: Arial, Helvetica, sans-serif; font-size: 16px; line-height: 24px; text-align: left; border-collapse: collapse; padding: 0 10px; margin: 0">
    <tbody>
    <tr>
        <td rowspan="15" style="width: 40px">&nbsp;</td>
        <td colspan="3" style="width: 520px">&nbsp;</td>
        <td rowspan="15" style="width: 40px">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3">Exciting news! 📣<br>Your data are in high demand on Dataland!
            <@formatFirstAndLastName requesterFirstName requesterLastName/> is requesting access to your data from ${companyName} on Dataland.</td>
    </tr>
    <@spacerRow/>
    <tr>
        <td colspan="3">
            <#include "../general/dataset_request_information.ftl">
        </td>
    </tr>
    <@spacerRow/>
    <@spacerRowTiny/>
    <@howToProceed items=["Verify the access request on Dataland.",
                          "If you want to share the dataset, grant access."]/>
    <@spacerRow/>
    <@buttonLink url="${baseUrl}/companyrequests" linkText="VERIFY AND GRANT ACCESS ON DATALAND" />
    <@spacerRow/>
    </tbody>
</table>
<#include "../general/why_me.ftl">
<#include "../general/footer.ftl">
</body>
</html>

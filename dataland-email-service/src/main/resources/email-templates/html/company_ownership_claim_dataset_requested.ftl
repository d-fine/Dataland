<#include "../general/general_makros_html.ftl">
<#include "../general/format_number_of_days.ftl">
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
            <@formatFirstAndLastName firstName lastName/> is requesting data from ${companyName}.</td>
    </tr>
    <@spacerRow/>
    <tr>
        <td colspan="3">
            <#include "../general/dataset_request_information.ftl">
        </td>
    </tr>
    <@spacerRow/>
    <@spacerRowTiny/>
    <@howToProceed items=["Unlock all your account features by claiming ownership of your company.",
                          "Provide your data."]/>
    <@spacerRowTiny/>
    <@buttonLink url="${baseUrl}/companies/${companyId}" linkText="REGISTER AND CLAIM OWNERSHIP" />
    <@spacerRow/>
    <tr>
        <td colspan="3">Claiming ownership process usually requires 1-2 business days.<br />
            You will be notified by email.</td>
    </tr>
    <@spacerRow/>
    </tbody>
</table>

<#include "../general/why_me.ftl">
<#include "../general/footer.ftl">
<#include "../general/unsubscribe.ftl">

</body>
</html>

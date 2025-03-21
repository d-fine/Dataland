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
        <td rowspan="25" style="width: 40px">&nbsp;</td>
        <td colspan="3" style="width: 520px">&nbsp;</td>
        <td rowspan="25" style="width: 40px">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3">
            Unfortunately, no public sources could be found for your requested dataset by a data provider.<br>
            We will continue to check the status of your request regularly and inform you in case the dataset will be uploaded in the future.
            If you are certain the requested data should exist, you may reopen your request on the
            <@textLink url="${baseUrl}/requests/${dataRequestId}" linkText="data request page"/>
        </td>
    </tr>
    <@spacerRow/>
    <@notificationSetting url="${baseUrl}/requests/${dataRequestId}"/>
    <@spacerRow/>
    <tr>
        <td colspan="3">
            <#include "../general/data_request_information.ftl">
        </td>
    </tr>
    <@spacerRow/>
    <@buttonLink url="${baseUrl}/requests/${dataRequestId}" linkText="REVIEW YOUR DATA REQUEST" />
    <@spacerRow/>
    </tbody>
</table>
<#include "../general/footer.ftl">

</body>
</html>

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>DATALAND</title>
</head>
<body style="background-color:#e3e3e3; height: 100%; margin: 0; padding: 0; width: 100%;">
<#include "./general/header.ftl">

<table style="background-color: #ffffff; width: 600px; font-family: Arial, Helvetica, sans-serif; font-size: 14px; text-align: left; border-collapse: collapse; padding: 0; margin: 0">
    <tbody>
    <tr>
        <td rowspan="15" style="width: 40px">&nbsp;</td>
        <td colspan="3" style="width: 520px">&nbsp;</td>
        <td rowspan="15" style="width: 40px">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3">Exciting news! 📣<br>Your data are in high demand on Dataland! A user is requesting data from ${companyName}.</td>
    </tr>
    <tr>
        <td colspan="3" style="height: 20px">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3">
            <table style="background-color: #f6f6f6; border-collapse: collapse; padding: 0; margin: 0; width: 520px">
                <tbody>
                <tr>
                    <td rowspan="18" style="width: 20px">&nbsp;</td>
                    <td style="width: 480px; height: 20px">&nbsp;</td>
                    <td rowspan="18" style="width: 20px">&nbsp;</td>
                </tr>
                <tr>
                    <td>Framework</td>
                </tr>
                <tr>
                    <td style="font-size: 5px; height: 5px">&nbsp;</td>
                </tr>
                <tr>
                    <td style="font-weight: bold; font-size:19px">${dataType}</td>
                </tr>
                <tr>
                    <td style="height: 20px">&nbsp;</td>
                </tr>
                <tr>
                    <td>Reporting year(s)</td>
                </tr>
                <tr>
                    <td style="font-size: 5px; height: 5px">&nbsp;</td>
                </tr>
                <tr>
                    <td style="font-weight: bold; font-size:19px">${reportingPeriods}</td>
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
                        <a href="mailto:${requesterEmail}" style="font-weight: bold; color: black; text-decoration: none; border: 0 none;">
                            ${requesterEmail}
                        </a>
                    </td>
                </tr>
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
                    <td style="font-weight: bold;">Hello Anna! I would really appreciate if you could provide the data asap.</td>
                </tr>
                <tr>
                    <td style="height: 20px">&nbsp;</td>
                </tr>
                </tbody>
            </table>
        </td>
    </tr>
    <tr>
        <td colspan="3" style="height: 40px">&nbsp;</td>
    </tr>

    <tr>
        <td colspan="3" style="font-weight: bold;">What to do now?</td>
    </tr>
    <tr>
        <td colspan="3" style="font-size: 5px; height: 5px">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3">
            <ol style="line-height: 25px;">
                <li>Unlock all your account features by <b>claiming ownership of your company.</b></li>
                <li><b>Provide your data.</b></li>
            </ol>
        </td>
    </tr>
    <tr>
        <td colspan="3" style="height: 20px">&nbsp;</td>
    </tr>
    <tr>
        <td style="text-align: left; padding:0; margin:0; border: 0; height: 54px; width: 26px">
            <img src="${baseUrl}/images/email/action_button_left_img.png" width="26" height="54" alt=""
                 style="border: 0 none; height: auto; line-height: 100%; display:block; outline: none; text-decoration: none;">
        </td>
        <td style="background-color: #ff5c00; text-align: center; padding:0; margin:0; border: 0; height: 54px; width: 468px;">
            <a href="${baseUrl}/companies/${companyId}" target="_blank" style="border: 0 none; line-height: 30px; color: #ffffff; font-size: 18px; width: 100%; display: block; text-decoration: none;">
                CALL TO ACTION
            </a>
        </td>
        <td style="background-color: #ffffff; text-align: right; padding:0; margin:0; border: 0; height: 54px; width: 26px">
            <img src="${baseUrl}/images/email/action_button_right_img.png" width="26" height="54" alt=""
                 style="border: 0 none; height: auto; line-height: 100%; display:block; outline: none; text-decoration: none;">
        </td>
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

<#include "./general/why_dataland.ftl">
<#include "./general/why_me.ftl">
<#include "./general/footer.ftl">

</body>
</html>

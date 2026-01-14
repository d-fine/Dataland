<#include "general_macros_html.ftl">
<#include "../general/base64_images.ftl">

<table id="footer" style="background-color: #1b1b1b; width: 600px; font-family: Arial, Helvetica, sans-serif; font-size: 18px; text-align: left; border-collapse: collapse; padding: 0; margin: 0; ; border: 0 none">
    <tbody>
    <tr>
        <td rowspan="20" style="width: 20px;">&nbsp;</td>
        <td colspan="3" style="width: 560px;">&nbsp;</td>
        <td rowspan="20" style="width: 20px;">&nbsp;</td>
    </tr>
    <@spacerRow/>
    <tr>
        <td colspan="3" style="text-align: left; background-color: #1b1b1b;border: 0 none">
            <a href="${baseUrl}" target="_blank" style="border: 0 none; text-decoration: none; color: #FF6813;">
                <img src="${darkLogo}" width="195" height="33" alt="DATALAND" style="border: 0 none; height: auto; line-height: 100%; outline: none; text-decoration: none;">
            </a>
        </td>
    </tr>
    <@spacerRow/>
    <@spacerRow/>
    <tr>
        <td style="color: #c0c0c0; font-size: 14px; border: 0 none">ESG Frameworks</td>
        <td style="color: #c0c0c0; font-size: 14px; border: 0 none">Follow Dataland</td>
        <td style="color: #c0c0c0; font-size: 14px; border: 0 none">&nbsp;</td>
    </tr>
    <@spacerRowSmall/>
    <tr>
        <@linkWithIcon url="https://github.com/d-fine/Dataland/wiki/Data-Framework-Documentation" linkText="OVERVIEW"/>
        <td style="line-height: 22px; border: 0 none">
            <img src="${linkedInImage}" width="22" height="23" alt="in"
                 style="border: 0 none; height: auto; line-height: 100%; outline: none; text-decoration: none; vertical-align: middle;">&nbsp;
            <a href="https://www.linkedin.com/company/dataland-gmbh/" target="_blank"
               style="border: 0 none; font-weight: bolder; font-size: 16px; color: #ffffff; text-decoration: none; vertical-align: middle;">
                LINKEDIN
            </a>
        </td>
        <td></td>
    </tr>

    <@spacerRowHorizontalLine color="#c0c0c0" width="560px"/>
    <@spacerRowSmall/>
    <tr>
        <td colspan="3" style="padding: 0">
            <table style="background-color: #1b1b1b; width: 560px; font-family: Arial, Helvetica, sans-serif; font-size: 18px; text-align: left; border-collapse: collapse; padding: 0 40px; margin: 0">
                <tbody>
                <tr>
                    <td rowspan="5" style="width: 20px; padding: 0">&nbsp;</td>
                    <td colspan="3" style="width: 520px; padding: 0">&nbsp;</td>
                    <td rowspan="5" style="width: 20px; padding: 0">&nbsp;</td>
                </tr>
                <tr>
                    <td style="padding: 0"><a href="${baseUrl}/terms" target="_blank" style="border: 0 none; font-weight: bolder; font-size: 16px; color: #ffffff; text-decoration: none;">LEGAL</a></td>
                    <td style="padding: 0"><a href="${baseUrl}/imprint" target="_blank" style="border: 0 none; font-weight: bolder; font-size: 16px; color: #ffffff; text-decoration: none;">IMPRINT</a></td>
                    <td style="padding: 0"><a href="${baseUrl}/dataprivacy" target="_blank" style="border: 0 none; font-weight: bolder; font-size: 16px; color: #ffffff; text-decoration: none;">DATA PRIVACY</a></td>
                </tr>
                <@spacerRow/>
                <tr>
                    <td colspan="3" style="padding: 0; height: 20px; font-size: 16px; color: #ffffff; text-decoration: none;">Copyright &copy; 2026 Dataland</td>
                </tr>
                </tbody>
                <@spacerRow/>
            </table>
        </td>
    </tr>
    </tbody>
</table>

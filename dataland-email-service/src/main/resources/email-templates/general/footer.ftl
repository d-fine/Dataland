<#include "../general/general_makros_html.ftl">
<#include "../general/base64_images.ftl">

<table id="footer" style="background-color: #1b1b1b; width: 600px; font-family: Arial, Helvetica, sans-serif; font-size: 18px; text-align: left; border-collapse: collapse; padding: 0; margin: 0; ">
    <tbody>
    <tr>
        <td rowspan="15" style="width: 20px;">&nbsp;</td>
        <td colspan="3" style="width: 560px;">&nbsp;</td>
        <td rowspan="15" style="width: 20px;">&nbsp;</td>
    </tr>
    <@spacerRow/>
    <tr>
        <td colspan="3" style="text-align: left; background-color: #1b1b1b;">
            <a href="${baseUrl}" target="_blank" style="border: 0 none; text-decoration: none; color: #FF6813;">
                <img src="${darkLogo}" width="195" height="33" alt="DATALAND" style="border: 0 none; height: auto; line-height: 100%; outline: none; text-decoration: none;">
            </a></td>
    </tr>
    <@spacerRow/>
    <@spacerRow/>
    <tr>
        <td style="color: #c0c0c0; font-size: 14px">Tech Hub</td>
        <td style="color: #c0c0c0; font-size: 14px">ESG Frameworks</td>
        <td style="color: #c0c0c0; font-size: 14px">&nbsp;</td>
    </tr>
    <@spacerRowSmall/>
    <tr>
        <@linkWithIcon url="${baseUrl}/api/swagger-ui/index.html" linkText="DATASETS"/>
        <@linkWithIcon url="https://github.com/d-fine/Dataland/wiki/Data-Framework-Documentation" linkText="OVERVIEW"/>
        <td></td>
    </tr>
    <@spacerRowSmall/>
    <tr>
        <@linkWithIcon url="${baseUrl}/documents/swagger-ui/index.html" linkText="DOCUMENTS"/>
        <td></td>
        <td></td>
    </tr>
    <@spacerRowSmall/>
    <tr>
        <@linkWithIcon url="${baseUrl}/community/swagger-ui/index.html" linkText="REQUESTS"/>
        <td style="color: #c0c0c0; font-size: 14px">Follow Dataland</td>
        <td></td>
    </tr>
    <@spacerRowSmall/>
    <tr>
        <td></td>
        <td style="line-height: 22px">
            <img src="${linkedInImage}" width="22" height="23" alt="in" style="border: 0 none; height: auto; line-height: 100%; outline: none; text-decoration: none; vertical-align: middle;">&nbsp;
            <a href="https://www.linkedin.com/company/dataland-gmbh/" target="_blank" style="border: 0 none; font-weight: bolder; font-size: 16px; color: #ffffff; text-decoration: none; vertical-align: middle;">LINKEDIN</a></td>
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
                    <td colspan="3" style="padding: 0; height: 20px; font-size: 16px; color: #ffffff; text-decoration: none;">Copyright &copy; 2024 Dataland</td>
                </tr>
                </tbody>
                <@spacerRow/>
            </table>
        </td>
    </tr>
    </tbody>
</table>
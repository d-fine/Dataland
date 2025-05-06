<#include "general_macros_html.ftl">

<table style="background-color: #ffffff; width: 600px; font-family: Arial, Helvetica, sans-serif; font-size: 16px; line-height: 24px; text-align: left; border-collapse: collapse; padding: 0; margin: 0">
    <tbody>
    <tr>
        <td rowspan="9" style="width: 40px;"></td>
        <td colspan="2" style="width: 520px; height: 20px"></td>
        <td rowspan="9" style="width: 40px;"></td>
    </tr>
    <@boldTitle title="Why me?"/>
    <@spacerRow/>
    <@bulletItemWithOptionalLink
        text="Due to our GLEIF collaboration, your company is already in Dataland."
        url="${baseUrl}/companies/${companyId}"
        linkText="CHECK IT HERE & CLAIM OWNERSHIP"/>
    <@spacerRow/>
    <@bulletItemWithOptionalLink
        text="Looks like your data are high on demand! Someone<br>needs your data to make valuable decisions."/>
    <@spacerRow/>
    <@bulletItemWithOptionalLink
        text="We need you in order to achieve our principles and mission."
        url="${baseUrl}/about"
        linkText="DISCOVER OUR PRINCIPLES"/>
    <@spacerRow/>
    <@spacerRow/>
    </tbody>
</table>
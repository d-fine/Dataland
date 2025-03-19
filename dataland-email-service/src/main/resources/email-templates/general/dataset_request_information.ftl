<#include "../general/general_makros_html.ftl">

<div style="background-color: #f6f6f6; padding: 20px; border-radius: 15px">
    <table style="background-color: #f6f6f6; border-collapse: collapse; margin: 0; width: 100%">
        <tbody>
        <@dataLabel label="Framework"/>
        <@spacerRowTiny/>
        <@dataValue value=dataTypeLabel/>
        <@spacerRow/>
        <@dataLabel label="Reporting year${(reportingPeriods?size > 1)?string('s', '')}"/>
        <@spacerRowTiny/>
        <@dataValue value=reportingPeriods?join(", ")/>
        <@spacerRowHorizontalLine position="bottom"/>
        <@spacerRow/>
        <tr>
            <td style="color:#8c8c8c">From:</td>
        </tr>
        <@spacerRowTiny/>
        <tr>
            <td style="font-weight: bold; color: #FF6813; text-decoration: none; border: 0 none;">
                <a href="mailto:${requesterEmail}" style="font-weight: bold; color: black; text-decoration: none; border: 0 none;">
                    ${requesterEmail}
                </a>
            </td>
        </tr>
        <#if message?has_content>
            <@spacerRow/>
            <tr>
                <td style="color:#8c8c8c">Message:</td>
            </tr>
            <@spacerRowTiny/>
            <tr>
                <td style="font-weight: bold;">${message}</td>
            </tr>
        </#if>
        </tbody>
    </table>
</div>

<#include "general_macros_html.ftl">

<table style="background-color: #f6f6f6; border-radius: 15px; border-collapse: collapse; padding: 0; margin: 0; width: 520px">
    <tbody>
        <tr>
            <td style="width: 20px;"> </td>
            <td>
                <table style="width: 480px;">
                    <tbody>
                        <@spacerRow/>
                        <@dataLabel label="Framework"/>
                        <@spacerRowTiny/>
                        <@dataValue value=dataTypeLabel/>
                        <@spacerRow/>
                        <@dataLabel label="Reporting year${(reportingPeriods?size > 1)?string('s', '')}"/>
                        <@spacerRowTiny/>
                        <@dataValue value=reportingPeriods?join(", ")/>
                        <@spacerRowHorizontalLine width="480px"/>
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
                        <@spacerRow/>
                    </tbody>
                </table>
            </td>
            <td style="width: 20px;"> </td>
        </tr>
    </tbody>
</table>

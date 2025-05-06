<#include "general_macros_html.ftl">

<table id="displayedDataRequest" style="background-color: #f6f6f6; border-radius: 15px; border-collapse: collapse; padding: 0; margin: 0; width: 520px">
    <tbody>
    <tr>
        <td style="width: 20px;"> </td>
        <td>
            <table style="width: 480px;">
                <tbody>
                    <@spacerRow/>
                    <@dataLabel label="Company"/>
                    <@spacerRowTiny/>
                    <@dataValue value="${companyName}"/>
                    <@spacerRow/>
                    <@dataLabel label="Framework"/>
                    <@spacerRowTiny/>
                    <@dataValue value="${dataTypeLabel}"/>
                    <@spacerRow/>
                    <@dataLabel label="Reporting period"/>
                    <@spacerRowTiny/>
                    <@dataValue value="${reportingPeriod}"/>
                    <@spacerRow/>
                    <@dataLabel label="Request created"/>
                    <@spacerRowTiny/>
                    <@dataValue value="${creationDate}"/>
                    <#if nonSourceableComment?? && nonSourceableComment?trim != "">
                        <@spacerRow/>
                        <@dataLabel label="Why are there no sources available"/>
                        <@spacerRowTiny/>
                        <@dataValue value="${nonSourceableComment}"/>
                    </#if>
                    <@spacerRow/>
                </tbody>
            </table>
        </td>
        <td style="width: 20px;"> </td>
    </table>
</div>
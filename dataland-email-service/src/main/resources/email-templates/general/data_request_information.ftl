<#include "../general/general_makros_html.ftl">

<div style="background-color: #f6f6f6; padding: 20px; border-radius: 15px">
    <table id="displayedDataRequest" style="background-color: #f6f6f6; border-collapse: collapse; margin: 0; width: 100%">
        <tbody>
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
        </tbody>
    </table>
</div>
Greetings!

You have been invited to provide data on Dataland.

People are interested in ${dataTypeLabel} data from ${companyName} for the year<#if (reportingPeriods?size > 1)>s</#if> ${reportingPeriods?join(", ")}.
<#if message?has_content>
    User ${requesterEmail} sent the following message:
    ${message}
</#if>

Register as a company owner on ${baseUrl}/companies/${companyId}

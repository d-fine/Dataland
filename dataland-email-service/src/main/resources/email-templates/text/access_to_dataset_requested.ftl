<#include "../general/format_first_and_last_name.ftl">
Great News!

Your data are in high demand on Dataland!
<@formatFirstAndLastName requesterFirstName requesterLastName/> is requesting access to your data from ${companyName} on Dataland.

The user is asking for your ${dataTypeLabel} data for the year<#if (reportingPeriods?size > 1)>s</#if>: ${reportingPeriods?join(", ")}.

<#if requesterEmail??>
You can contact the user with their Email-Address ${requesterEmail}.
</#if>

<#if message??>
The user also sent the following message:
${message}.
</#if>

You can verify the access request and grant access to your data on Dataland:
${baseUrl}/companies/${companyId}.

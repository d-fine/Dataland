<#include "../general/format_number_of_days.ftl">
<#include "../general/format_first_and_last_name.ftl">
Exciting news! 📣

Your data are in high demand on Dataland!
<@formatFirstAndLastName firstName lastName/> is requesting data from ${companyName}.

Framework Details:
Framework: ${dataTypeLabel}
Reporting year<#if (reportingPeriods?size > 1)>s</#if>: ${reportingPeriods?join(", ")}

<#if requesterEmail??>
You can contact the user with their Email-Address ${requesterEmail}.
</#if>

<#if message??>
The user also sent the following message:
${message}.
</#if>

How to proceed?
1. Unlock all your account features by claiming ownership of your company.
2. Provide your data.

REGISTER AND CLAIM OWNERSHIP:
${baseUrl}/companies/${companyId}

Claiming ownership process usually requires 1-2 business days.
You will be notified by email.

<#include "../general/unsubscribe_text.ftl">
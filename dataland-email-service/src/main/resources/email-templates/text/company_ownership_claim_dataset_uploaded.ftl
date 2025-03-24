<#include "../general/format_number_of_days.ftl">
Exciting news! 📣
<#if (frameworkData?size > 1)>Multiple datasets<#else>A dataset</#if> for ${companyName} have been uploaded to Dataland.

<#list frameworkData as framework>
${framework.dataTypeLabel}: ${framework.reportingPeriods?join(", ")}
</#list>

How to proceed?
1. Gain sovereignty over your data by claiming company ownership.
2. Inspect, add, correct, remove data of your company.

CLAIM COMPANY OWNERSHIP:
${baseUrl}/companies/${companyId}

Claiming ownership process usually requires 1-2 business days.
You will be notified by email.

<#include "../general/unsubscribe_text.ftl">

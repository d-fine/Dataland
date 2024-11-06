<#include "../general/format_number_of_days.ftl">

Exciting news! 📣
Multiple datasets for ${companyName} have been uploaded to Dataland <@formatNumberOfDays numberOfDays/>.

<#list frameworkData as framework>
    ${framework.dataType}: ${framework.reportingPeriods?join(", ")}
</#list>

How to proceed?
1. Gain sovereignty over your data by claiming company ownership.
2. Inspect, add, correct, remove data of your company.

Click the link below to claim company ownership:
${baseUrl}/companies/${companyId}

Claiming ownership process usually requires 1-2 business days.
You will be notified by email.

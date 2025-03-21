<#import "../general/general_makros_text.ftl" as text>
Your data request has been updated with new data.

<@text.immediateNotification url="${baseUrl}/requests/${dataRequestId}"/>

Company: ${companyName}
Framework: ${dataTypeLabel}
Reporting period: ${reportingPeriod}
Request created: ${creationDate}

View your data request:
${baseUrl}/requests/${dataRequestId}
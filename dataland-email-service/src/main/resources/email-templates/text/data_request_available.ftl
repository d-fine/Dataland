<#import "../general/general_makros_text.ftl" as text>
Great news!

Your data request has been answered.

<@text.immediateNotification url="${baseUrl}/requests/${dataRequestId}" />

Company: ${companyName}
Framework: ${dataTypeLabel}
Reporting period: ${reportingPeriod}
Request created: ${creationDate}

<@text.howToProceed />

Review your data request:
${baseUrl}/requests/${dataRequestId}

Without any actions, your data request will be set to closed automatically in ${closedInDays} days.
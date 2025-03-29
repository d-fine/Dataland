<#import "../general/general_makros_text.ftl" as text>
Your data request has been updated with new data.

<@text.immediateNotification url="${baseUrl}/requests/${dataRequestId}"/>

<#include "../general/data_request_information_text.ftl">

View your data request:
${baseUrl}/requests/${dataRequestId}
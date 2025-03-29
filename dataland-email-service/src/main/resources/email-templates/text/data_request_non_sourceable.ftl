<#import "../general/general_makros_text.ftl" as text>
Unfortunately, no public sources could be found for your requested dataset by a data provider.
We will continue to check the status of your request regularly and inform you in case the dataset will be uploaded in the future.

<@text.immediateNotification url="${baseUrl}/requests/${dataRequestId}"/>

If you are certain the requested data should exist, you may reopen your request on the data request page:
${baseUrl}/requests/${dataRequestId}

<#include "../general/data_request_information_text.ftl">
Reason unavailable: ${nonSourceableComment}

View your data request:
${baseUrl}/requests/${dataRequestId}
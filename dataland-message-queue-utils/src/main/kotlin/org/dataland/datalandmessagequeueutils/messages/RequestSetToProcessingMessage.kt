package org.dataland.datalandmessagequeueutils.messages

/**
 * Message that is sent from Data Sourcing Service to Accounting Service when a request is set to state Processing.
 * @param triggeringUserId the ID of the user whose request is set to Processing
 * @param dataSourcingId the ID of the associated data sourcing object
 * @param requestedCompanyId the ID of the company whose data is requested
 * @param requestedReportingPeriod the reporting period for which data is requested
 * @param requestedFramework the requested data framework
 */
data class RequestSetToProcessingMessage(
    val triggeringUserId: String,
    val dataSourcingId: String,
    val requestedCompanyId: String,
    val requestedReportingPeriod: String,
    val requestedFramework: String,
)

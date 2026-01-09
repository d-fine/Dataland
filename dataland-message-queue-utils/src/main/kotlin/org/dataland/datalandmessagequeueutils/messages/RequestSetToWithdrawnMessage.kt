package org.dataland.datalandmessagequeueutils.messages

/**
 * Message that is sent from Data Sourcing Service to Accounting Service when a request is set from state Processed or Processing
 * to state Withdrawn.
 * @param triggeringUserId the ID of the user whose request is set to Withdrawn
 * @param dataSourcingId the ID of the associated data sourcing object
 * @param requestedCompanyId the ID of the company whose data is requested
 * @param requestedReportingPeriod the reporting period for which data is requested
 * @param requestedFramework the requested data framework
 * @param userIdsAssociatedRequestsForSameTriple the list of user ids of requests associated with the data sourcing object
 */
data class RequestSetToWithdrawnMessage(
    val triggeringUserId: String,
    val dataSourcingId: String,
    val requestedCompanyId: String,
    val requestedReportingPeriod: String,
    val requestedFramework: String,
    val userIdsAssociatedRequestsForSameTriple: List<String>,
)
